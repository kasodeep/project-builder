package fury.deep.project_builder.events;

import fury.deep.project_builder.repository.analytics.AnalyticsMapper;
import fury.deep.project_builder.repository.project.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// todo: review left.
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsListener {

    private static final int MAX_TASKS_PER_USER = 5;

    private final AnalyticsMapper mapper;
    private final ProjectMapper projectMapper;

    /* ===================================================== */
    /* ================= TASK CREATED ====================== */
    /* ===================================================== */

    @Async
    @EventListener
    public void onTaskCreated(TaskCreatedEvent e) {
        String projectId = e.projectId();
        ensureProject(projectId);

        recomputeFlow(projectId);          // WIP changes
        recomputeDependency(projectId);    // dependency density changes
        recomputeHealth(projectId);        // score depends on above

        recomputeTeamByProject(projectId); // workload increases
    }

    /* ===================================================== */
    /* ================= TASK UPDATED ====================== */
    /* ===================================================== */

    @Async
    @EventListener
    public void onTaskUpdated(TaskUpdatedEvent e) {
        String projectId = e.projectId();
        ensureProject(projectId);

        recomputeFlow(projectId);
        recomputeHealth(projectId);
    }

    /* ===================================================== */
    /* ================= TASK DELETED ====================== */
    /* ===================================================== */

    @Async
    @EventListener
    public void onTaskDeleted(TaskDeletedEvent e) {
        String projectId = e.projectId();
        ensureProject(projectId);

        recomputeFlow(projectId);
        recomputeDependency(projectId);
        recomputeHealth(projectId);

        recomputeTeamByProject(projectId);
    }

    /* ===================================================== */
    /* ================= STATUS CHANGED ==================== */
    /* ===================================================== */

    @Async
    @EventListener
    public void onStatusChanged(TaskStatusChangedEvent e) {

        String projectId = e.projectId();
        ensureProject(projectId);

        recomputeFlow(projectId);      // throughput + WIP
        recomputeDependency(projectId);// blocked deps change
        recomputeHealth(projectId);
        recomputeProgress(projectId);  // only here

        recomputeTeamByProject(projectId);
    }

    /* ===================================================== */
    /* ================= DEPENDENCY CHANGED ================ */
    /* ===================================================== */

    @Async
    @EventListener
    public void onDependenciesChanged(TaskDependenciesReplacedEvent e) {
        String projectId = e.projectId();
        ensureProject(projectId);

        recomputeDependency(projectId);
        recomputeHealth(projectId);
    }

    /* ===================================================== */
    /* ================= ASSIGNEE CHANGED ================== */
    /* ===================================================== */

    @Async
    @EventListener
    public void onAssigneesChanged(TaskAssigneesReplacedEvent e) {
        recomputeTeamByProject(e.projectId());
    }

    /* ===================================================== */
    /* ================= INTERNAL HELPERS ================== */
    /* ===================================================== */

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

    /* ===================================================== */
    /* ================= FLOW METRICS ====================== */
    /* ===================================================== */

    /**
     * Flow Model:
     * <p>
     * WIP              → active tasks (not completed)
     * Throughput 7d    → completed in last 7 days
     * Throughput 30d   → completed in last 30 days
     * Avg Cycle Time   → avg(completed_at - started_at)
     * <p>
     * We deliberately ignore tasks without both timestamps.
     */
    private void recomputeFlow(String projectId) {

        int wip = mapper.countActiveWip(projectId);
        int t7 = mapper.countThroughput7d(projectId);
        int t30 = mapper.countThroughput30d(projectId);

        double avgCycleRaw = mapper.calculateAvgCycleTime(projectId);
        int avgCycleDays = (int) Math.round(avgCycleRaw);

        mapper.updateFlow(projectId, wip, t7, t30, avgCycleDays);
    }

    /* ===================================================== */
    /* ================= DEPENDENCY RISK =================== */
    /* ===================================================== */

    /**
     * Dependency Risk Model:
     * <p>
     * Density = total_dependencies / total_tasks
     * <p>
     * Risk score formula (bounded 0-100):
     * <p>
     * 40% blocked dependency ratio
     * 30% density
     * 30% critical path depth
     */
    private void recomputeDependency(String projectId) {

        int totalTasks = mapper.countTasks(projectId);
        int totalDeps = mapper.countDependencies(projectId);
        int blockedDeps = mapper.countBlockedDependencies(projectId);
        int criticalPath = mapper.calculateCriticalPath(projectId);

        double density = totalTasks == 0
                ? 0
                : (double) totalDeps / totalTasks;

        double blockedRatio = totalDeps == 0
                ? 0
                : (double) blockedDeps / totalDeps;

        int riskScore = (int) Math.round(
                (blockedRatio * 40)
                        + (density * 30)
                        + (Math.min(criticalPath, 10) * 3)
        );

        riskScore = Math.min(100, riskScore);

        mapper.updateDependencyRisk(
                projectId,
                totalDeps,
                blockedDeps,
                criticalPath,
                density,
                riskScore
        );
    }

    /* ===================================================== */
    /* ================= TEAM CAPACITY ===================== */
    /* ===================================================== */

    /**
     * Burnout model:
     * <p>
     * overloadedUsers → 60% weight
     * avgTasks/user   → 40% weight
     * <p>
     * normalized to 0-100
     */
    private void recomputeTeam(String teamId) {

        int activeTasks = mapper.countActiveTasksByTeam(teamId);
        int activeProjects = mapper.countActiveProjectsByTeam(teamId);

        double avgTasksRaw = mapper.calculateAvgTasksPerUser(teamId);
        int avgTasks = (int) Math.round(avgTasksRaw);

        int overloaded = mapper.countOverloadedUsers(teamId, MAX_TASKS_PER_USER);

        double avgCompletionRaw = mapper.calculateAvgCompletionTime(teamId);
        int avgCompletion = (int) Math.round(avgCompletionRaw);

        int burnoutScore = (int) Math.round(
                (overloaded * 15)
                        + (avgTasks * 5)
        );

        burnoutScore = Math.min(100, burnoutScore);

        mapper.updateTeamCapacity(
                teamId,
                activeProjects,
                activeTasks,
                avgTasks,
                overloaded,
                avgCompletion,
                burnoutScore
        );
    }

    /* ===================================================== */
    /* ================= PROJECT HEALTH ==================== */
    /* ===================================================== */

    /**
     * Health model:
     * <p>
     * Score = 100
     * - 5 × overdue_tasks
     * - 3 × blocked_dependencies
     * - 2 × high_cycle_time_penalty
     * <p>
     * Risk Levels:
     * GREEN ≥ 80
     * AMBER ≥ 55
     * RED   < 55
     */
    private void recomputeHealth(String projectId) {

        int overdue = mapper.countOverdue(projectId);
        int blockedDeps = mapper.countBlockedDependencies(projectId);

        int highCyclePenalty =
                mapper.countLongRunningTasks(projectId);

        int score = 100
                - (overdue * 5)
                - (blockedDeps * 3)
                - (highCyclePenalty * 2);

        score = Math.max(0, score);

        String risk =
                score >= 80 ? "GREEN"
                        : score >= 55 ? "AMBER"
                        : "RED";

        mapper.updateProjectHealth(
                projectId,
                overdue * 2,
                overdue,
                blockedDeps,
                score,
                risk
        );
    }

    private void recomputeProgress(String projectId) {
        projectMapper.recomputeProjectProgress(projectId);
    }
}
