package fury.deep.project_builder.service.task;

import fury.deep.project_builder.dto.task.util.AddAssigneeRequest;
import fury.deep.project_builder.dto.task.util.AddDependenciesRequest;
import fury.deep.project_builder.entity.task.Task;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.events.TaskAssigneesReplacedEvent;
import fury.deep.project_builder.events.TaskDependenciesReplacedEvent;
import fury.deep.project_builder.repository.UserMapper;
import fury.deep.project_builder.repository.task.TaskUtilMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides utility methods for tasks to replace assignees and dependencies.
 *
 * @author night_fury_44
 */
@Service
public class TaskUtilService {

    private final TaskUtilMapper taskUtilMapper;
    private final TaskService taskService;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher publisher;

    public TaskUtilService(TaskUtilMapper taskUtilMapper, TaskService taskService, UserMapper userMapper, ApplicationEventPublisher publisher) {
        this.taskUtilMapper = taskUtilMapper;
        this.taskService = taskService;
        this.userMapper = userMapper;
        this.publisher = publisher;
    }

    /**
     * The method finds the task, and also validate the access internally by tS.
     * Constraint check that all tasks must belong to the same project.
     * It checks for each dependency, do we create a cycle or not.
     */
    @Transactional
    public void addDependencies(AddDependenciesRequest request, User user) {
        Task task = taskService.findById(request.taskId(), user);

        int validCount = taskService.countTasksInProject(
                request.dependencies(),
                task.getProjectId()
        );
        if (validCount != request.dependencies().size()) {
            throw new IllegalArgumentException(
                    "All tasks must belong to the same project as the base task"
            );
        }

        for (String depId : request.dependencies()) {
            boolean createsCycle = taskUtilMapper.createsCycle(task.getId(), depId);
            if (createsCycle) {
                throw new IllegalStateException("Dependency cycle detected");
            }
        }

        taskUtilMapper.replaceDependencies(task.getId(), request.dependencies(), task.getProjectId());
        publisher.publishEvent(
                new TaskDependenciesReplacedEvent(
                        task.getId(),
                        task.getProjectId()
                )
        );
    }

    /**
     * The method validates the tasks.
     * It checks that all users must belong to the same team.
     */
    @Transactional
    public void addAssignees(AddAssigneeRequest request, User user) {
        Task task = taskService.findById(request.taskId(), user); // also validates the user.

        int validCount = userMapper.countUsersInTeam(request.assignees(), user.getTeamId());
        if (validCount != request.assignees().size()) {
            throw new IllegalArgumentException(
                    "All managers must belong to the same team as the project"
            );
        }

        taskUtilMapper.replaceAssignees(task.getId(), request.assignees(), user.getTeamId());
        publisher.publishEvent(
                new TaskAssigneesReplacedEvent(
                        task.getId(),
                        task.getProjectId()
                )
        );
    }
}
