package fury.deep.project_builder.service.task;

import fury.deep.project_builder.constants.AggregateType;
import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.dto.task.CreateTaskRequest;
import fury.deep.project_builder.dto.task.UpdateTaskRequest;
import fury.deep.project_builder.entity.task.Feature;
import fury.deep.project_builder.entity.task.Status;
import fury.deep.project_builder.entity.task.Task;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.events.*;
import fury.deep.project_builder.exception.ResourceNotFoundException;
import fury.deep.project_builder.exception.UnAuthorizedException;
import fury.deep.project_builder.repository.task.TaskMapper;
import fury.deep.project_builder.service.FeatureService;
import fury.deep.project_builder.service.outbox.OutboxService;
import fury.deep.project_builder.service.project.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Service related to tasks to provide CRUD operations with validations and status updates.
 * A major event firing machine, cause each task related service call triggers multiple events.
 *
 * @author night_fury_44
 */
@Service
public class TaskService {

    private final TaskMapper taskMapper;
    private final ProjectService projectService;
    private final FeatureService featureService;
    private final OutboxService outboxService;

    public TaskService(TaskMapper taskMapper, ProjectService projectService, FeatureService featureService, OutboxService outboxService) {
        this.taskMapper = taskMapper;
        this.projectService = projectService;
        this.featureService = featureService;
        this.outboxService = outboxService;
    }

    /**
     * The method validated the access to project and feature.
     * Then it creates the task and first the event.
     */
    @Transactional
    public void createTask(CreateTaskRequest request, User user) {
        projectService.validateAccess(request.projectId(), user);
        Feature feature = featureService.findById(request.featureId());

        Task task = buildNewTask(request, user, feature);
        applyInitialLifecycle(task);
        taskMapper.insertTask(task);

        outboxService.save(
                AggregateType.TASK,
                task.getId(),
                new TaskCreatedEvent(
                        task.getId(),
                        task.getProjectId(),
                        task.getStatus()
                )
        );
    }

    /**
     * TODO: Apply concurrency solve. Check really updating.
     * TODO: Updates should be allowed by assignee.
     * The method updates the tasks, applies the status transition.
     * It fires the events based on the condition, of status change or static.
     */
    @Transactional
    public void updateTask(UpdateTaskRequest request, User user) {
        Task existing = findById(request.id(), user);
        Feature feature = featureService.findById(request.featureId());

        Status oldStatus = existing.getStatus();
        Status newStatus = request.status();

        validateTransition(oldStatus, newStatus);
        applyLifecycleTransition(existing, oldStatus, newStatus);

        boolean hasChanges = hasChanges(existing, request, feature);

        existing.setName(request.name());
        existing.setFeature(feature);
        existing.setPriority(request.priority());
        existing.setStart(request.start());
        existing.setEnd(request.end());
        existing.setStatus(newStatus);
        existing.setUpdatedAt(Instant.now());
        existing.setUpdatedBy(user.getUsername());

        taskMapper.updateTask(existing);

        if (hasChanges) {
            publishStatusAwareEvent(existing, oldStatus, newStatus);
        }
    }

    private boolean hasChanges(Task existing, UpdateTaskRequest request, Feature feature) {
        return !existing.getName().equals(request.name())
                || !existing.getStatus().equals(request.status())
                || !existing.getFeature().getId().equals(feature.getId())
                || !Objects.equals(existing.getPriority(), request.priority())
                || !Objects.equals(existing.getStart(), request.start())
                || !Objects.equals(existing.getEnd(), request.end());
    }

    @Transactional
    public void deleteTask(String taskId, User user) {
        Task existing = findById(taskId, user);
        taskMapper.deleteTask(taskId);

        outboxService.save(
                AggregateType.TASK,
                existing.getId(),
                new TaskDeletedEvent(existing.getId(), existing.getProjectId())
        );
    }

    public Task findById(String taskId, User user) {
        Task task = taskMapper.findTaskById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorMessages.TASK_NOT_FOUND.formatted(taskId));
        }

        projectService.validateAccess(task.getProjectId(), user);
        return task;
    }

    // TODO: Add filters and proper date or status. Or maybe we can change to project wise fetch.
    public List<Task> tasksForUser(User user) {
        return taskMapper.findTasksByUserId(user.getId());
    }

    public List<Task> findTasksByProjectId(String projectId, User user) {
        projectService.validateAccess(projectId, user);
        return taskMapper.findTasksByProjectId(projectId);
    }

    public int countTasksInProject(List<String> tasks, String projectId) {
        return taskMapper.countTasksInProject(tasks, projectId);
    }

    @Transactional
    public void completeTask(String taskId, User user) {
        Task task = taskMapper.findTaskById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorMessages.TASK_NOT_FOUND.formatted(taskId));
        }

        // Only assignees can complete
        List<String> assignees = taskMapper.findAssigneesByTaskId(taskId);
        if (!assignees.contains(user.getId())) {
            throw new UnAuthorizedException("Only an assignee can mark this task as completed.");
        }

        // Must be ACTIVE to complete
        validateTransition(task.getStatus(), Status.COMPLETED);

        task.setStatus(Status.COMPLETED);
        task.setCompletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        task.setUpdatedBy(user.getUsername());
        taskMapper.updateTask(task);

        outboxService.save(
                AggregateType.TASK,
                task.getId(),
                new TaskStatusChangedEvent(task.getId(), task.getProjectId(), Status.ACTIVE, Status.COMPLETED)
        );

        // Unlock dependents
        unlockDependents(task);
    }

    private void unlockDependents(Task completedTask) {
        List<Task> dependents = taskMapper.findTasksDependingOn(completedTask.getId());

        for (Task dependent : dependents) {
            if (dependent.getStatus() != Status.LOCKED) continue;

            // Check all of THIS task's dependencies are completed
            List<String> depIds = taskMapper.findDependenciesByTaskId(dependent.getId());
            boolean allCompleted = taskMapper.countCompletedTasks(depIds) == depIds.size();

            if (allCompleted) {
                dependent.setStatus(Status.PENDING);
                dependent.setUpdatedAt(Instant.now());
                taskMapper.updateTask(dependent);

                outboxService.save(
                        AggregateType.TASK,
                        dependent.getId(),
                        new TaskStatusChangedEvent(dependent.getId(), dependent.getProjectId(), Status.LOCKED, Status.PENDING)
                );
            }
        }
    }

    /**
     * Updates the task when it is created depending on the status.
     */
    private void applyInitialLifecycle(Task task) {
        Instant now = Instant.now();

        if (task.getStatus() == Status.ACTIVE) {
            task.setStartedAt(now);
        }

        if (task.getStatus() == Status.COMPLETED) {
            task.setStartedAt(now);
            task.setCompletedAt(now);
        }
    }

    /**
     * Applies the transition during the update.
     */
    private void applyLifecycleTransition(Task task, Status oldStatus, Status newStatus) {
        if (oldStatus == newStatus) {
            return;
        }

        Instant now = Instant.now();
        // PENDING → ACTIVE
        if (oldStatus == Status.PENDING && newStatus == Status.ACTIVE) {
            if (task.getStartedAt() == null) {
                task.setStartedAt(now);
            }
        }

        // ACTIVE → COMPLETED
        if (oldStatus == Status.ACTIVE && newStatus == Status.COMPLETED) {
            task.setCompletedAt(now);
        }

        // COMPLETED → ACTIVE (Reopen)
        if (oldStatus == Status.COMPLETED && newStatus == Status.ACTIVE) {
            task.setCompletedAt(null);
        }
    }

    private static final Map<Status, Set<Status>> ALLOWED_TRANSITIONS = Map.of(
            Status.LOCKED, EnumSet.of(Status.PENDING),
            Status.PENDING, EnumSet.of(Status.ACTIVE, Status.ARCHIVED),
            Status.ACTIVE, EnumSet.of(Status.COMPLETED, Status.PENDING),
            Status.COMPLETED, EnumSet.of(Status.ACTIVE, Status.ARCHIVED),
            Status.ARCHIVED, EnumSet.noneOf(Status.class)
    );

    /**
     * Validates the transition of task status from one to another.
     */
    private void validateTransition(Status oldStatus, Status newStatus) {
        if (oldStatus == newStatus) {
            return;
        }

        Set<Status> allowed = ALLOWED_TRANSITIONS.getOrDefault(oldStatus, EnumSet.noneOf(Status.class));
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException("Illegal status transition: " + oldStatus + " → " + newStatus);
        }
    }

    /**
     * Fires status changed event on changes in status, otherwise, fires the task update event.
     */
    private void publishStatusAwareEvent(Task task, Status oldStatus, Status newStatus) {
        if (oldStatus != newStatus) {
            outboxService.save(
                    AggregateType.TASK,
                    task.getId(),
                    new TaskStatusChangedEvent(task.getId(), task.getProjectId(), oldStatus, newStatus)
            );
        } else {
            outboxService.save(
                    AggregateType.TASK,
                    task.getId(),
                    new TaskUpdatedEvent(task.getId(), task.getProjectId(), newStatus)
            );
        }
    }

    private Task buildNewTask(CreateTaskRequest request, User user, Feature feature) {
        return Task.builder()
                .name(request.name())
                .projectId(request.projectId())
                .feature(feature)
                .priority(request.priority())
                .status(request.status())
                .start(request.start())
                .end(request.end())
                .updatedAt(Instant.now())
                .updatedBy(user.getUsername())
                .build();
    }
}
