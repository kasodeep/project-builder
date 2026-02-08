package fury.deep.project_builder.events;

import fury.deep.project_builder.entity.task.Status;
import fury.deep.project_builder.repository.analytics.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 * Listener to update the project analytics when different events are fired.
 * It uses a separate analytics mapper for separation of responsibility.
 *
 * @author night_fury_44
 */
@Service
@RequiredArgsConstructor
@EnableAsync
public class AnalyticsListener {

    private final AnalyticsMapper mapper;

    /**
     * The method handles the task created event.
     * It updated the project analytics, feature analytics and the status count metric.
     */
    @Async
    @EventListener
    public void onTaskCreated(TaskCreatedEvent e) {
        mapper.ensureProject(e.projectId());
        mapper.incTotalTasks(e.projectId());
        mapper.incStatus(e.projectId(), e.status().name());

        if (e.status() == Status.COMPLETED) {
            mapper.incCompletedTasks(e.projectId());
        }

        String featureId = mapper.findFeatureIdByTask(e.taskId());
        if (featureId != null) {
            mapper.ensureFeature(featureId);
            mapper.incFeatureTotal(featureId);

            if (e.status() == Status.COMPLETED) {
                mapper.incFeatureCompleted(featureId);
            }
        }
    }

    /**
     * The method handles the status changed, to update the analytics.
     */
    @Async
    @EventListener
    public void onStatusChanged(TaskStatusChangedEvent e) {
        mapper.decStatus(e.projectId(), e.oldStatus().name());
        mapper.incStatus(e.projectId(), e.newStatus().name());

        String featureId = mapper.findFeatureIdByTask(e.taskId());

        if (e.oldStatus() != Status.COMPLETED && e.newStatus() == Status.COMPLETED) {
            mapper.incCompletedTasks(e.projectId());
            mapper.incFeatureCompleted(featureId);
        }

        if (e.oldStatus() == Status.COMPLETED && e.newStatus() != Status.COMPLETED) {
            mapper.decCompletedTasks(e.projectId());
            mapper.decFeatureCompleted(featureId);
        }
    }

    /**
     * The method handles the dependencies changed event for a task.
     * It updates the blocked tasks for project and feature.
     */
    @Async
    @EventListener
    public void onDependenciesChanged(TaskDependenciesReplacedEvent e) {
        mapper.updateBlockedTasks(
                e.projectId(),
                mapper.countBlockedTasks(e.projectId())
        );

        String featureId = mapper.findFeatureIdByTask(e.taskId());
        if (featureId != null) {
            mapper.updateFeatureBlocked(
                    featureId,
                    mapper.countBlockedTasksByFeature(featureId)
            );
        }

        for (String userId : mapper.findUsersInProject(e.projectId())) {
            mapper.ensureUserRisk(userId);
            mapper.updateUserRisk(
                    userId,
                    mapper.countBlockingTasksForUser(userId),
                    mapper.countBlockedUsersForUser(userId)
            );
        }
    }

    @Async
    @EventListener
    public void onAssigneesChanged(TaskAssigneesReplacedEvent e) {
        for (String userId : mapper.findUsersAssignedToTask(e.taskId())) {
            mapper.ensureUserRisk(userId);
            mapper.updateUserRisk(
                    userId,
                    mapper.countBlockingTasksForUser(userId),
                    mapper.countBlockedUsersForUser(userId)
            );
        }
    }
}

