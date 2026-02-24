package fury.deep.project_builder.service.task;

import fury.deep.project_builder.constants.AggregateType;
import fury.deep.project_builder.dto.task.util.AddAssigneeRequest;
import fury.deep.project_builder.dto.task.util.AddDependenciesRequest;
import fury.deep.project_builder.entity.task.Task;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.events.TaskAssigneesReplacedEvent;
import fury.deep.project_builder.events.TaskDependenciesReplacedEvent;
import fury.deep.project_builder.repository.UserMapper;
import fury.deep.project_builder.repository.task.TaskUtilMapper;
import fury.deep.project_builder.service.outbox.OutboxService;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides utility methods for tasks to replace assignees and dependencies.
 *
 * @author night_fury_44
 */
@Slf4j
@Service
public class TaskUtilService {

    private final TaskUtilMapper taskUtilMapper;
    private final TaskService taskService;
    private final UserMapper userMapper;
    private final OutboxService outboxService;

    public TaskUtilService(TaskUtilMapper taskUtilMapper, TaskService taskService, UserMapper userMapper, OutboxService outboxService) {
        this.taskUtilMapper = taskUtilMapper;
        this.taskService = taskService;
        this.userMapper = userMapper;
        this.outboxService = outboxService;
    }

    /**
     * Replaces task dependencies after validating:
     * <ol>
     *   <li>All dependency task IDs belong to the same project.</li>
     *   <li>No dependency would create a cycle.</li>
     * </ol>
     */
    @Transactional
    @Observed(name = "task.addDependencies", contextualName = "addDependencies")
    public void addDependencies(AddDependenciesRequest request, User user) {
        Task task = taskService.findById(request.taskId(), user);

        int validCount = taskService.countTasksInProject(
                request.dependencies(), task.getProjectId());
        if (validCount != request.dependencies().size()) {
            log.warn("Dependency project mismatch taskId={} requested={} valid={}",
                    task.getId(), request.dependencies().size(), validCount);
            throw new IllegalArgumentException(
                    "All tasks must belong to the same project as the base task");
        }

        for (String depId : request.dependencies()) {
            boolean createsCycle = taskUtilMapper.createsCycle(task.getId(), depId);
            if (createsCycle) {
                log.warn("Cycle detected taskId={} dependencyId={}", task.getId(), depId);
                throw new IllegalStateException("Dependency cycle detected");
            }
        }

        taskUtilMapper.replaceDependencies(task.getId(), request.dependencies(), task.getProjectId());

        log.info("Dependencies replaced taskId={} count={} user={}",
                task.getId(), request.dependencies().size(), user.getUsername());

        taskService.evictTaskById(request.taskId());
        taskService.evictTasksByProject(task.getProjectId());
        taskService.evictTasksForUser(user.getId());

        outboxService.save(AggregateType.TASK, task.getId(),
                new TaskDependenciesReplacedEvent(task.getId(), task.getProjectId()));
    }

    /**
     * Replaces task assignees after validating all users belong to the same team.
     */
    @Transactional
    @Observed(name = "task.addAssignees", contextualName = "addAssignees")
    public void addAssignees(AddAssigneeRequest request, User user) {
        Task task = taskService.findById(request.taskId(), user);

        int validCount = userMapper.countUsersInTeam(request.assignees(), user.getTeamId());
        if (validCount != request.assignees().size()) {
            log.warn("Assignee team mismatch taskId={} requested={} validInTeam={}",
                    task.getId(), request.assignees().size(), validCount);
            throw new IllegalArgumentException(
                    "All assignees must belong to the same team as the task");
        }

        taskUtilMapper.replaceAssignees(task.getId(), request.assignees(), user.getTeamId());

        log.info("Assignees replaced taskId={} count={} user={}",
                task.getId(), request.assignees().size(), user.getUsername());

        taskService.evictTaskById(request.taskId());
        taskService.evictTasksByProject(task.getProjectId());
        taskService.evictTasksForUser(user.getId());

        outboxService.save(AggregateType.TASK, task.getId(),
                new TaskAssigneesReplacedEvent(task.getId(), task.getProjectId()));
    }
}