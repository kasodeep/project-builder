package fury.deep.project_builder.events;

import fury.deep.project_builder.entity.task.Status;
import fury.deep.project_builder.repository.analytics.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@EnableAsync
public class AnalyticsListener {

    private final AnalyticsMapper mapper;

    /* ---------- TASK CREATED ---------- */
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
        }
    }

    /* ---------- STATUS CHANGE ---------- */
    @Async
    @EventListener
    public void onStatusChanged(TaskStatusChangedEvent e) {

        mapper.decStatus(e.projectId(), e.oldStatus().name());
        mapper.incStatus(e.projectId(), e.newStatus().name());

        if (e.oldStatus() != Status.COMPLETED
                && e.newStatus() == Status.COMPLETED) {

            mapper.incCompletedTasks(e.projectId());
        }

        if (e.oldStatus() == Status.COMPLETED
                && e.newStatus() != Status.COMPLETED) {
            mapper.decCompletedTasks(e.projectId());
        }
    }

    /* ---------- DEPENDENCIES ---------- */
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

    /* ---------- ASSIGNEES ---------- */
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

