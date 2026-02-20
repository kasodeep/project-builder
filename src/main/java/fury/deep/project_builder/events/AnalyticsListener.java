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
 * the transactional thread that published the event.  Each handler calls
 * {@link #ensureProject(String)} first so that target rows always exist
 * before any UPDATE is attempted — including for the first event ever fired
 * for a given project.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsListener {

    /**
     * A user carrying more than this many active tasks is considered overloaded.
     * Passed directly to the SQL query so the value is never duplicated.
     */
    static final int OVERLOAD_THRESHOLD = 5;

    private final AnalyticsMapper mapper;
    private final ProjectMapper projectMapper;

    @Async
    @EventListener
    public void onTaskCreated(TaskCreatedEvent e) {
        String projectId = e.projectId();
        log.debug("analytics: task created in project={}", projectId);
        ensureProject(projectId);

        recomputeFlow(projectId);
        recomputeDependency(projectId);
        recomputeHealth(projectId);
        recomputeTeamByProject(projectId);
    }

    @Async
    @EventListener
    public void onTaskUpdated(TaskUpdatedEvent e) {
        String projectId = e.projectId();
        log.debug("analytics: task updated in project={}", projectId);
        ensureProject(projectId);

        recomputeFlow(projectId);
        recomputeHealth(projectId);
        recomputeTeamByProject(projectId);
    }

    @Async
    @EventListener
    public void onTaskDeleted(TaskDeletedEvent e) {
        String projectId = e.projectId();
        log.debug("analytics: task deleted in project={}", projectId);
        ensureProject(projectId);

        recomputeFlow(projectId);
        recomputeDependency(projectId);
        recomputeHealth(projectId);
        recomputeTeamByProject(projectId);
    }

    @Async
    @EventListener
    public void onStatusChanged(TaskStatusChangedEvent e) {
        String projectId = e.projectId();
        log.debug("analytics: status changed in project={}", projectId);
        ensureProject(projectId);

        recomputeFlow(projectId);
        recomputeDependency(projectId);
        recomputeHealth(projectId);
        recomputeProgress(projectId);
        recomputeTeamByProject(projectId);
    }

    @Async
    @EventListener
    public void onDependenciesChanged(TaskDependenciesReplacedEvent e) {
        String projectId = e.projectId();
        log.debug("analytics: dependencies replaced in project={}", projectId);
        ensureProject(projectId);

        recomputeDependency(projectId);
        recomputeHealth(projectId);
    }

    @Async
    @EventListener
    public void onAssigneesChanged(TaskAssigneesReplacedEvent e) {
        String projectId = e.projectId();
        log.debug("analytics: assignees replaced in project={}", projectId);
        ensureProject(projectId);

        recomputeTeamByProject(projectId);
    }

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

    /**
     * Flow model:
     * <ul>
     *   <li>WIP            — active tasks (status ≠ COMPLETED)</li>
     *   <li>Throughput 7d  — completed in the last 7 days</li>
     *   <li>Throughput 30d — completed in the last 30 days</li>
     *   <li>Avg cycle time — avg(completed_at − started_at) in days;
     *                        tasks missing either timestamp are excluded</li>
     * </ul>
     */
    private void recomputeFlow(String projectId) {
        FlowAggRow row = mapper.selectFlowAgg(projectId);
        mapper.updateFlow(projectId, row.getWip(), row.getT7(), row.getT30(), row.getAvgCycle());
    }

    /**
     * Dependency risk model (score 0–100):
     * <ul>
     *   <li>40 pts — blocked-dependency ratio (blocked / total deps)</li>
     *   <li>30 pts — dependency density (total deps / total tasks)</li>
     *   <li>30 pts — critical-path depth (capped at 10 hops → 3 pts each)</li>
     * </ul>
     */
    private void recomputeDependency(String projectId) {
        DependencyAggRow row = mapper.selectDependencyAgg(projectId);

        double blockedRatio = row.getTotal() == 0
                ? 0.0
                : (double) row.getBlocked() / row.getTotal();

        int risk = (int) Math.round(
                (blockedRatio * 40)
                        + (row.getDensity() * 30)
                        + (Math.min(row.getCriticalPath(), 10) * 3)
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

    /**
     * Burnout model (score 0–100):
     * <pre>
     *   score = (overloadedUsers / totalUsers) × 60
     *         + (avgTasksPerUser / OVERLOAD_THRESHOLD) × 40
     * </pre>
     * Both components are clamped to their maximum contribution before summing
     * so a single extreme value cannot push the total above 100.
     *
     * <p>This matches the documented 60/40 weight split and is properly
     * normalized. The previous implementation used raw multipliers (×15 and
     * ×5) that produced unbounded values with no relationship to the weights
     * described in the Javadoc.
     */
    private void recomputeTeam(String teamId) {
        TeamAggRow row = mapper.selectTeamAgg(teamId, OVERLOAD_THRESHOLD);

        // Guard against division-by-zero when a team has no active tasks yet.
        double overloadedRatio = row.getActiveTasks() == 0
                ? 0.0
                : (double) row.getOverloaded() / Math.max(row.getActiveTasks(), 1);

        double avgTasksRatio = OVERLOAD_THRESHOLD == 0
                ? 0.0
                : (double) row.getAvgTasks() / OVERLOAD_THRESHOLD;

        int burnoutScore = (int) Math.round(
                Math.min(overloadedRatio, 1.0) * 60
                        + Math.min(avgTasksRatio, 1.0) * 40
        );

        mapper.updateTeamCapacity(
                teamId,
                row.getActiveProjects(),
                row.getActiveTasks(),
                row.getAvgTasks(),
                row.getOverloaded(),
                row.getAvgCompletion(),
                Math.min(100, burnoutScore)
        );
    }

    /**
     * Health model:
     * <pre>
     *   score = 100
     *         − 5 × overdue_tasks
     *         − 3 × blocked_dependencies
     *         − 2 × long_running_tasks        (started > 14 days ago, not done)
     * </pre>
     * Risk levels: GREEN ≥ 80 · AMBER ≥ 55 · RED &lt; 55
     */
    private void recomputeHealth(String projectId) {
        HealthAggRow row = mapper.selectHealthAgg(projectId);

        int score = 100
                - (row.getOverdue() * 5)
                - (row.getBlocked() * 3)
                - (row.getLongRunning() * 2);

        score = Math.max(0, score);

        String riskLevel = score >= 80 ? "GREEN"
                : score >= 55 ? "AMBER"
                : "RED";

        mapper.updateProjectHealth(
                projectId,
                row.getOverdue(),
                row.getBlocked(),
                score,
                riskLevel
        );
    }

    private void recomputeProgress(String projectId) {
        projectMapper.recomputeProjectProgress(projectId);
    }
}