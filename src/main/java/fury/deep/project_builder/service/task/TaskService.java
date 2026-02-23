package fury.deep.project_builder.service.task;

import fury.deep.project_builder.config.CacheConfig;
import fury.deep.project_builder.constants.AggregateType;
import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.dto.task.CreateTaskRequest;
import fury.deep.project_builder.dto.task.UpdateTaskRequest;
import fury.deep.project_builder.entity.task.Feature;
import fury.deep.project_builder.entity.task.Status;
import fury.deep.project_builder.entity.task.Task;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.events.*;
import fury.deep.project_builder.exception.OptimisticLockException;
import fury.deep.project_builder.exception.ResourceNotFoundException;
import fury.deep.project_builder.exception.UnAuthorizedException;
import fury.deep.project_builder.repository.task.TaskMapper;
import fury.deep.project_builder.service.FeatureService;
import fury.deep.project_builder.service.outbox.OutboxService;
import fury.deep.project_builder.service.project.ProjectService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Service related to tasks to provide CRUD operations with validations and status updates.
 * A major event firing machine — each task service call triggers multiple outbox events.
 *
 * @author night_fury_44
 */
@Slf4j
@Service
public class TaskService {

    private final TaskMapper taskMapper;
    private final ProjectService projectService;
    private final FeatureService featureService;
    private final OutboxService outboxService;

    // ── Metrics ──────────────────────────────────────────────────────────────
    private final Counter taskCreatedCounter;
    private final Counter taskDeletedCounter;
    private final Counter taskCompletedCounter;
    private final Counter taskUpdateConflictCounter;
    private final MeterRegistry meterRegistry;   // kept for tagged transition counters

    public TaskService(TaskMapper taskMapper,
                       ProjectService projectService,
                       FeatureService featureService,
                       OutboxService outboxService,
                       MeterRegistry meterRegistry) {
        this.taskMapper = taskMapper;
        this.projectService = projectService;
        this.featureService = featureService;
        this.outboxService = outboxService;
        this.meterRegistry = meterRegistry;

        this.taskCreatedCounter = Counter.builder("task.created.total")
                .description("Total tasks created")
                .register(meterRegistry);
        this.taskDeletedCounter = Counter.builder("task.deleted.total")
                .description("Total tasks deleted")
                .register(meterRegistry);
        this.taskCompletedCounter = Counter.builder("task.completed.total")
                .description("Total tasks completed")
                .register(meterRegistry);
        this.taskUpdateConflictCounter = Counter.builder("task.update.conflict.total")
                .description("Optimistic lock conflicts during task update")
                .register(meterRegistry);
    }

    /**
     * Validates access to the project and feature, then creates the task and fires the outbox event.
     */
    @Transactional
    @Observed(name = "task.create", contextualName = "createTask")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.TASKS_BY_PROJECT, key = "#request.projectId"),
            @CacheEvict(value = CacheConfig.TASKS_FOR_USER,   key = "#user.id")
    })
    public void createTask(CreateTaskRequest request, User user) {
        projectService.validateAccess(request.projectId(), user);
        Feature feature = featureService.findById(request.featureId());

        Task task = buildNewTask(request, user, feature);
        applyInitialLifecycle(task);
        taskMapper.insertTask(task);

        taskCreatedCounter.increment();
        log.info("Task created taskId={} projectId={} status={} user={}",
                task.getId(), task.getProjectId(), task.getStatus(), user.getUsername());

        outboxService.save(
                AggregateType.TASK,
                task.getId(),
                new TaskCreatedEvent(task.getId(), task.getProjectId(), task.getStatus())
        );
    }

    /**
     * Updates the task, applies the status transition, and fires the appropriate outbox event.
     * Uses optimistic locking — throws {@link OptimisticLockException} when the supplied
     * {@code version} doesn't match the current DB row (concurrent edit detected).
     */
    @Transactional
    @Observed(name = "task.update", contextualName = "updateTask")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.TASK_BY_ID,       key = "#request.id"),
            @CacheEvict(value = CacheConfig.TASKS_BY_PROJECT, key = "#existing.projectId"),
            @CacheEvict(value = CacheConfig.TASKS_FOR_USER,   key = "#user.id")
    })
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
        existing.setVersion(request.version());

        int rowsUpdated = taskMapper.updateTask(existing);

        if (rowsUpdated == 0) {
            taskUpdateConflictCounter.increment();
            log.warn("Optimistic lock conflict taskId={} suppliedVersion={} user={}",
                    request.id(), request.version(), user.getUsername());
            throw new OptimisticLockException(
                    "Task was modified by another request. Please refresh and retry.");
        }

        log.info("Task updated taskId={} oldStatus={} newStatus={} user={}",
                request.id(), oldStatus, newStatus, user.getUsername());

        if (oldStatus != newStatus) {
            Counter.builder("task.status.transition.total")
                    .tag("from", oldStatus.name())
                    .tag("to", newStatus.name())
                    .description("Task status transitions")
                    .register(meterRegistry)
                    .increment();
        }

        if (hasChanges) {
            publishStatusAwareEvent(existing, oldStatus, newStatus);
        }
    }

    @Transactional
    @Observed(name = "task.delete", contextualName = "deleteTask")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.TASK_BY_ID,       key = "#taskId"),
            @CacheEvict(value = CacheConfig.TASKS_FOR_USER,   key = "#user.id")
    })
    public void deleteTask(String taskId, User user) {
        Task existing = findById(taskId, user);
        taskMapper.deleteTask(taskId);

        taskDeletedCounter.increment();
        log.info("Task deleted taskId={} projectId={} user={}",
                taskId, existing.getProjectId(), user.getUsername());

        outboxService.save(
                AggregateType.TASK,
                existing.getId(),
                new TaskDeletedEvent(existing.getId(), existing.getProjectId())
        );
    }

    /**
     * Marks a task as COMPLETED. Only assignees may do this.
     * After completing, attempts to unlock any dependent tasks whose other dependencies are all done.
     */
    @Transactional
    @Observed(name = "task.complete", contextualName = "completeTask")
    public void completeTask(String taskId, User user) {
        Task task = taskMapper.findTaskById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorMessages.TASK_NOT_FOUND.formatted(taskId));
        }

        List<String> assignees = taskMapper.findAssigneesByTaskId(taskId);
        if (!assignees.contains(user.getId())) {
            log.warn("Unauthorized complete attempt taskId={} user={}", taskId, user.getUsername());
            throw new UnAuthorizedException("Only an assignee can mark this task as completed.");
        }

        validateTransition(task.getStatus(), Status.COMPLETED);

        task.setStatus(Status.COMPLETED);
        task.setCompletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        task.setUpdatedBy(user.getUsername());

        int rowsUpdated = taskMapper.updateTask(task);
        if (rowsUpdated == 0) {
            taskUpdateConflictCounter.increment();
            log.warn("Optimistic lock conflict on completeTask taskId={} user={}", taskId, user.getUsername());
            throw new OptimisticLockException(
                    "Task was modified concurrently. Please refresh and retry.");
        }

        evictTaskById(taskId);
        evictTasksByProject(task.getProjectId());
        evictTasksForUser(user.getId());

        taskCompletedCounter.increment();
        log.info("Task completed taskId={} projectId={} user={}",
                taskId, task.getProjectId(), user.getUsername());

        outboxService.save(
                AggregateType.TASK,
                task.getId(),
                new TaskStatusChangedEvent(task.getId(), task.getProjectId(), Status.ACTIVE, Status.COMPLETED)
        );

        unlockDependents(task);
    }

    @Cacheable(value = CacheConfig.TASK_BY_ID, key = "#taskId")
    @Observed(name = "task.findById", contextualName = "findTaskById")
    public Task findById(String taskId, User user) {
        Task task = taskMapper.findTaskById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorMessages.TASK_NOT_FOUND.formatted(taskId));
        }
        projectService.validateAccess(task.getProjectId(), user);
        return task;
    }

    @Cacheable(value = CacheConfig.TASKS_FOR_USER, key = "#user.id")
    public List<Task> tasksForUser(User user) {
        return taskMapper.findTasksByUserId(user.getId());
    }

    @Cacheable(value = CacheConfig.TASKS_BY_PROJECT, key = "#projectId")
    public List<Task> findTasksByProjectId(String projectId, User user) {
        projectService.validateAccess(projectId, user);
        return taskMapper.findTasksByProjectId(projectId);
    }

    public int countTasksInProject(List<String> tasks, String projectId) {
        return taskMapper.countTasksInProject(tasks, projectId);
    }

    @CacheEvict(value = CacheConfig.TASK_BY_ID, key = "#taskId")
    public void evictTaskById(String taskId) {}

    @CacheEvict(value = CacheConfig.TASKS_BY_PROJECT, key = "#projectId")
    public void evictTasksByProject(String projectId) {}

    @CacheEvict(value = CacheConfig.TASKS_FOR_USER, key = "#userId")
    public void evictTasksForUser(String userId) {}

    // ── PRIVATE ───────────────────────────────────────────────────────────────

    private void unlockDependents(Task completedTask) {
        List<Task> dependents = taskMapper.findTasksDependingOn(completedTask.getId());

        for (Task dependent : dependents) {
            if (dependent.getStatus() != Status.LOCKED) continue;

            List<String> depIds = taskMapper.findDependenciesByTaskId(dependent.getId());
            boolean allCompleted = taskMapper.countCompletedTasks(depIds) == depIds.size();

            if (allCompleted) {
                dependent.setStatus(Status.PENDING);
                dependent.setUpdatedAt(Instant.now());

                int rows = taskMapper.updateTask(dependent);
                if (rows == 0) {
                    // Log and skip — another process may have already unlocked it
                    log.warn("Skipped unlock for dependent taskId={} — version conflict",
                            dependent.getId());
                    continue;
                }

                log.info("Dependent task unlocked taskId={} projectId={}",
                        dependent.getId(), dependent.getProjectId());

                outboxService.save(
                        AggregateType.TASK,
                        dependent.getId(),
                        new TaskStatusChangedEvent(
                                dependent.getId(), dependent.getProjectId(),
                                Status.LOCKED, Status.PENDING)
                );
            }
        }
    }

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

    private void applyLifecycleTransition(Task task, Status oldStatus, Status newStatus) {
        if (oldStatus == newStatus) return;
        Instant now = Instant.now();

        if (oldStatus == Status.PENDING && newStatus == Status.ACTIVE) {
            if (task.getStartedAt() == null) task.setStartedAt(now);
        }
        if (oldStatus == Status.ACTIVE && newStatus == Status.COMPLETED) {
            task.setCompletedAt(now);
        }
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

    private void validateTransition(Status oldStatus, Status newStatus) {
        if (oldStatus == newStatus) return;
        Set<Status> allowed = ALLOWED_TRANSITIONS.getOrDefault(oldStatus, EnumSet.noneOf(Status.class));
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException("Illegal status transition: " + oldStatus + " → " + newStatus);
        }
    }

    private void publishStatusAwareEvent(Task task, Status oldStatus, Status newStatus) {
        if (oldStatus != newStatus) {
            outboxService.save(AggregateType.TASK, task.getId(),
                    new TaskStatusChangedEvent(task.getId(), task.getProjectId(), oldStatus, newStatus));
        } else {
            outboxService.save(AggregateType.TASK, task.getId(),
                    new TaskUpdatedEvent(task.getId(), task.getProjectId(), newStatus));
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