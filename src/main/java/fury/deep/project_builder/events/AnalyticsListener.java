package fury.deep.project_builder.events;

import fury.deep.project_builder.entity.task.Status;
import fury.deep.project_builder.repository.analytics.AnalyticsMapper;
import fury.deep.project_builder.repository.project.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 * Central analytics projection listener.
 * <p>
 * Listens to all AnalyticsEvent implementations and
 * updates read-model metrics asynchronously.
 * <p>
 * Handles:
 * - Flow (WIP, throughput)
 * - Dependency risk
 * - Team capacity
 * - Project health
 * - Project progress
 * <p>
 * This acts as a lightweight CQRS projection layer.
 * <p>
 * All operations are idempotent recomputations.
 *
 * @author night_fury_44
 */
@Service
@RequiredArgsConstructor
@EnableAsync
public class AnalyticsListener {

    private final AnalyticsMapper mapper;
    private final ProjectMapper projectMapper;

    /* ===================================================== */
    /* ================== TASK CREATED ===================== */
    /* ===================================================== */

    /**
     * Fired after a task is created.
     * <p>
     * Updates:
     * - Ensures analytics aggregates exist
     * - Flow (WIP)
     * - Team capacity
     * - Project health
     * - Progress
     */
    @Async
    @EventListener
    public void onTaskCreated(TaskCreatedEvent e) {

        ensureAnalyticsRows(e.projectId());

        String teamId = mapper.findTeamByProject(e.projectId());
        mapper.ensureTeamCapacity(teamId);

        recomputeFlow(e.projectId());
        recomputeTeam(teamId);
        recomputeHealth(e.projectId());
        recomputeProgress(e.projectId());
    }

    /* ===================================================== */
    /* ================== TASK UPDATED ===================== */
    /* ===================================================== */

    /**
     * Fired when task metadata changes
     * (priority, dates, feature, etc.)
     * <p>
     * Updates:
     * - Flow
     * - Health
     * - Progress
     */
    @Async
    @EventListener
    public void onTaskUpdated(TaskUpdatedEvent e) {
        recomputeFlow(e.projectId());
        recomputeHealth(e.projectId());
        recomputeProgress(e.projectId());
    }

    /* ===================================================== */
    /* ================== TASK DELETED ===================== */
    /* ===================================================== */

    /**
     * Fired after a task is deleted.
     * <p>
     * Updates:
     * - Flow
     * - Dependency risk
     * - Team capacity
     * - Health
     * - Progress
     */
    @Async
    @EventListener
    public void onTaskDeleted(TaskDeletedEvent e) {

        ensureAnalyticsRows(e.projectId());

        String teamId = mapper.findTeamByProject(e.projectId());
        mapper.ensureTeamCapacity(teamId);

        recomputeFlow(e.projectId());
        recomputeDependency(e.projectId());
        recomputeTeam(teamId);
        recomputeHealth(e.projectId());
        recomputeProgress(e.projectId());
    }

    /* ===================================================== */
    /* ============== STATUS CHANGED ======================= */
    /* ===================================================== */

    /**
     * Fired when task status transitions.
     * <p>
     * Updates:
     * - Throughput (if COMPLETED)
     * - Flow
     * - Health
     * - Progress
     */
    @Async
    @EventListener
    public void onStatusChanged(TaskStatusChangedEvent e) {

        if (e.newStatus() == Status.COMPLETED) {
            mapper.incrementThroughput(e.projectId());
        }

        String teamId = mapper.findTeamByProject(e.projectId());

        recomputeDependency(e.projectId());
        recomputeFlow(e.projectId());
        recomputeTeam(teamId);      // 🔥 missing earlier
        recomputeHealth(e.projectId());
        recomputeProgress(e.projectId());
    }

    /* ===================================================== */
    /* ============ DEPENDENCIES REPLACED ================== */
    /* ===================================================== */

    /**
     * Fired when a task's dependency list is replaced.
     * <p>
     * Updates:
     * - Dependency density
     * - Blocked count
     * - Health
     */
    @Async
    @EventListener
    public void onDependenciesChanged(TaskDependenciesReplacedEvent e) {
        recomputeDependency(e.projectId());
        recomputeHealth(e.projectId());
    }

    /* ===================================================== */
    /* ============== ASSIGNEES REPLACED =================== */
    /* ===================================================== */

    /**
     * Fired when task assignees are replaced.
     * <p>
     * Updates:
     * - Team capacity
     * - Health
     */
    @Async
    @EventListener
    public void onAssigneesChanged(TaskAssigneesReplacedEvent e) {

        String teamId = mapper.findTeamByProject(e.projectId());
        mapper.ensureTeamCapacity(teamId);

        recomputeTeam(teamId);
        recomputeHealth(e.projectId());
    }

    /* ===================================================== */
    /* ================= SHARED HELPERS ==================== */
    /* ===================================================== */

    /**
     * Ensures analytics projection rows exist.
     */
    private void ensureAnalyticsRows(String projectId) {
        mapper.ensureProjectHealth(projectId);
        mapper.ensureFlow(projectId);
        mapper.ensureDependency(projectId);
    }

    /**
     * Recomputes Work-In-Progress (WIP).
     */
    private void recomputeFlow(String projectId) {
        int wip = mapper.countWip(projectId);
        mapper.updateWip(projectId, wip);
    }

    /**
     * Recomputes dependency risk metrics.
     */
    private void recomputeDependency(String projectId) {

        int total = mapper.countDependencies(projectId);
        int blocked = mapper.countBlockedDependencies(projectId);

        double density = total == 0 ? 0.0 : (double) total / 100.0;
        int risk = blocked * 5;

        mapper.updateDependencyRisk(projectId, total, blocked, density, risk);
    }

    /**
     * Recomputes team workload and burnout risk.
     */
    private void recomputeTeam(String teamId) {

        int active = mapper.countActiveTasksByTeam(teamId);
        int avg = active;
        int overloaded = active > 20 ? 1 : 0;
        int burnout = overloaded * 25;

        mapper.updateTeamCapacity(teamId, active, avg, overloaded, burnout);
    }

    /**
     * Recomputes overall project health score.
     */
    private void recomputeHealth(String projectId) {

        int overdue = mapper.countOverdue(projectId);
        int blocked = mapper.countBlockedDependencies(projectId);

        int variance = overdue * 2;

        int score = 100 - (overdue * 5) - (blocked * 3);
        if (score < 0) score = 0;

        String risk =
                score > 75 ? "GREEN" :
                        score > 50 ? "AMBER" :
                                "RED";

        mapper.updateProjectHealth(projectId, variance, overdue, blocked, score, risk);
    }

    /**
     * Recomputes project completion percentage.
     */
    private void recomputeProgress(String projectId) {
        int progress = projectMapper.calculateProjectProgress(projectId);
        projectMapper.updateProjectProgress(projectId, progress);
    }
}
