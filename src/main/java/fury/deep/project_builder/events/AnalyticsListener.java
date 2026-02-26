package fury.deep.project_builder.events;

import fury.deep.project_builder.dto.analytics.db.DependencyAggRow;
import fury.deep.project_builder.dto.analytics.db.FlowAggRow;
import fury.deep.project_builder.dto.analytics.db.HealthAggRow;
import fury.deep.project_builder.dto.analytics.db.TeamAggRow;
import fury.deep.project_builder.repository.analytics.AnalyticsMapper;
import fury.deep.project_builder.repository.project.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Listens to domain events and keeps analytics aggregates consistent.
 *
 * <p>All handlers run on a dedicated async thread pool so they never block
 * the transactional thread that published the event.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsListener {

    /**
     * A user carrying more than this many active tasks is considered overloaded.
     */
    static final int OVERLOAD_THRESHOLD = 5;

    private final AnalyticsMapper mapper;
    private final ProjectMapper projectMapper;

    // ─── Event handlers ──────────────────────────────────────────────────────

    @Async
    @EventListener
    public void onTaskCreated(TaskCreatedEvent e) {
        log.debug("analytics: task created in project={}", e.projectId());
        ensureProject(e.projectId());
        recomputeFlow(e.projectId());
        recomputeDependency(e.projectId());
        recomputeHealth(e.projectId());
        recomputeTeamByProject(e.projectId());
        recomputeProgress(e.projectId());
    }

    @Async
    @EventListener
    public void onTaskUpdated(TaskUpdatedEvent e) {
        log.debug("analytics: task updated in project={}", e.projectId());
        ensureProject(e.projectId());
        recomputeFlow(e.projectId());
        recomputeHealth(e.projectId());
        recomputeTeamByProject(e.projectId());
    }

    @Async
    @EventListener
    public void onTaskDeleted(TaskDeletedEvent e) {
        log.debug("analytics: task deleted in project={}", e.projectId());
        ensureProject(e.projectId());
        recomputeFlow(e.projectId());
        recomputeDependency(e.projectId());
        recomputeHealth(e.projectId());
        recomputeTeamByProject(e.projectId());
        recomputeProgress(e.projectId());
    }

    @Async
    @EventListener
    public void onStatusChanged(TaskStatusChangedEvent e) {
        log.debug("analytics: status changed in project={}", e.projectId());
        ensureProject(e.projectId());
        recomputeFlow(e.projectId());
        recomputeDependency(e.projectId());
        recomputeHealth(e.projectId());
        recomputeProgress(e.projectId());
        recomputeTeamByProject(e.projectId());
    }

    @Async
    @EventListener
    public void onDependenciesChanged(TaskDependenciesReplacedEvent e) {
        log.debug("analytics: dependencies replaced in project={}", e.projectId());
        ensureProject(e.projectId());
        recomputeDependency(e.projectId());
        recomputeHealth(e.projectId());
    }

    @Async
    @EventListener
    public void onAssigneesChanged(TaskAssigneesReplacedEvent e) {
        log.debug("analytics: assignees replaced in project={}", e.projectId());
        ensureProject(e.projectId());
        recomputeTeamByProject(e.projectId());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void ensureProject(String projectId) {
        mapper.ensureProjectHealth(projectId);
        mapper.ensureFlow(projectId);
        mapper.ensureDependency(projectId);

        String teamId = mapper.findTeamByProject(projectId);
        if (teamId != null) {
            mapper.ensureTeamCapacity(teamId);
        }
    }

    private void recomputeTeamByProject(String projectId) {
        String teamId = mapper.findTeamByProject(projectId);
        if (teamId != null) {
            recomputeTeam(teamId);
        }
    }

    // ─── Flow ────────────────────────────────────────────────────────────────

    /**
     * Flow model:
     * <ul>
     *   <li>WIP            — active tasks (status ≠ COMPLETED)</li>
     *   <li>Throughput 7d  — completed in the last 7 days</li>
     *   <li>Throughput 30d — completed in the last 30 days</li>
     *   <li>Avg cycle time — avg(completed_at − started_at) in days as double;
     *                        tasks missing either timestamp excluded</li>
     * </ul>
     */
    private void recomputeFlow(String projectId) {
        FlowAggRow row = mapper.selectFlowAgg(projectId);
        mapper.updateFlow(projectId, row.getWip(), row.getT7(), row.getT30(), row.getAvgCycle());
    }

    // ─── Dependency risk ─────────────────────────────────────────────────────

    /**
     * Dependency risk score (0–100):
     * <ul>
     *   <li>40 pts — blocked ratio (blocked deps / total deps, capped at 1.0)</li>
     *   <li>30 pts — normalised density (total deps / max possible edges n*(n-1)/2)</li>
     *   <li>30 pts — critical path depth (capped at 10 hops → 3 pts each)</li>
     * </ul>
     *
     * <p>Density is now normalised against max possible edges so small and
     * large projects are treated proportionally instead of small projects
     * being penalised more heavily.
     */
    private void recomputeDependency(String projectId) {
        DependencyAggRow row = mapper.selectDependencyAgg(projectId);

        double blockedRatio = row.getTotal() == 0
                ? 0.0
                : (double) row.getBlocked() / row.getTotal();

        int risk = (int) Math.round(
                Math.min(blockedRatio, 1.0) * 40
                        + Math.min(row.getDensity(), 1.0) * 30
                        + Math.min(row.getCriticalPath(), 10) * 3.0
        );

        mapper.updateDependencyRisk(
                projectId,
                row.getTotal(),
                row.getBlocked(),
                row.getCriticalPath(),
                row.getDensity(),
                Math.min(100, risk)
        );
    }

    // ─── Health ──────────────────────────────────────────────────────────────

    /**
     * Health grade thresholds (penalty = 5×overdue + 3×blocked + 2×longRunning):
     * <pre>
     *   A  penalty = 0        perfect
     *   B  penalty  1 –  9    minor issues
     *   C  penalty 10 – 19    needs attention
     *   D  penalty 20 – 34    at risk
     *   F  penalty ≥ 35       critical
     * </pre>
     * <p>
     * risk_level stays consistent: A/B → GREEN · C → AMBER · D/F → RED
     *
     * <p>longRunning is now stored in the DB (was computed but silently dropped).
     */
    private void recomputeHealth(String projectId) {
        HealthAggRow row = mapper.selectHealthAgg(projectId);

        int penalty = (row.getOverdue() * 5)
                + (row.getBlocked() * 3)
                + (row.getLongRunning() * 2);

        String grade;
        if (penalty == 0) grade = "A";
        else if (penalty < 10) grade = "B";
        else if (penalty < 20) grade = "C";
        else if (penalty < 35) grade = "D";
        else grade = "F";

        String riskLevel = switch (grade) {
            case "A", "B" -> "GREEN";
            case "C" -> "AMBER";
            default -> "RED";       // D or F
        };

        mapper.updateProjectHealth(
                projectId,
                row.getOverdue(),
                row.getBlocked(),
                row.getLongRunning(),
                grade,
                riskLevel
        );
    }

    // ─── Team capacity ───────────────────────────────────────────────────────

    /**
     * Burnout score (0–100):
     * <pre>
     *   overloadedRatio = overloadedUsers / activeUsers   (fraction of PEOPLE overloaded)
     *   avgTasksRatio   = avgTasksPerUser / OVERLOAD_THRESHOLD
     *
     *   score = min(overloadedRatio, 1.0) × 60
     *         + min(avgTasksRatio,   1.0) × 40
     * </pre>
     *
     * <p>Previously divided by {@code activeTasks} instead of {@code activeUsers},
     * producing a nonsensical ratio (e.g. 2 overloaded users / 20 tasks = 10%
     * instead of 2 / 3 users = 67%).  Fixed to use {@code activeUsers}.
     *
     * <p>avgTasks and avgCompletion are now passed as {@code double} so the
     * NUMERIC precision from Postgres is retained end-to-end.
     */
    private void recomputeTeam(String teamId) {
        TeamAggRow row = mapper.selectTeamAgg(teamId, OVERLOAD_THRESHOLD);

        // Use activeUsers (people) as denominator, not activeTasks
        double overloadedRatio = row.getActiveUsers() == 0
                ? 0.0
                : (double) row.getOverloaded() / row.getActiveUsers();

        double avgTasksRatio = OVERLOAD_THRESHOLD == 0
                ? 0.0
                : row.getAvgTasks() / OVERLOAD_THRESHOLD;

        int burnout = (int) Math.round(
                Math.min(overloadedRatio, 1.0) * 60
                        + Math.min(avgTasksRatio, 1.0) * 40
        );

        mapper.updateTeamCapacity(
                teamId,
                row.getActiveProjects(),
                row.getActiveTasks(),
                row.getAvgTasks(),       // double
                row.getOverloaded(),
                row.getAvgCompletion(),  // double
                Math.min(100, burnout)
        );
    }

    private void recomputeProgress(String projectId) {
        projectMapper.recomputeProjectProgress(projectId);
    }
}