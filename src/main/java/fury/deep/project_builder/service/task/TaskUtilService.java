package fury.deep.project_builder.service.task;

import fury.deep.project_builder.dto.task.util.AddAssigneeRequest;
import fury.deep.project_builder.dto.task.util.AddDependenciesRequest;
import fury.deep.project_builder.entity.task.Task;
import fury.deep.project_builder.entity.task.TaskDependency;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.repository.UserMapper;
import fury.deep.project_builder.repository.task.TaskUtilMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static fury.deep.project_builder.graph.CycleDetection.buildGraph;
import static fury.deep.project_builder.graph.CycleDetection.checkForCycle;

@Service
public class TaskUtilService {

    private final TaskUtilMapper taskUtilMapper;
    private final TaskService taskService;
    private final UserMapper userMapper;

    public TaskUtilService(TaskUtilMapper taskUtilMapper, TaskService taskService, UserMapper userMapper) {
        this.taskUtilMapper = taskUtilMapper;
        this.taskService = taskService;
        this.userMapper = userMapper;
    }

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

        List<TaskDependency> existing = taskUtilMapper.findAllDependenciesByProjectId(task.getProjectId());

        Map<String, List<String>> graph = buildGraph(existing, task.getId(), request.dependencies());
        checkForCycle(graph);

        taskUtilMapper.replaceDependencies(
                task.getId(),
                request.dependencies(),
                task.getProjectId()
        );
    }


    @Transactional
    public void addAssignees(AddAssigneeRequest request, User user) {
        Task task = taskService.findById(request.taskId(), user); // also validates the user.

        int validCount = userMapper.countUsersInTeam(request.assignees(), user.getTeam().getId());
        if (validCount != request.assignees().size()) {
            throw new IllegalArgumentException(
                    "All managers must belong to the same team as the project"
            );
        }

        taskUtilMapper.replaceAssignees(task.getId(), request.assignees(), user.getTeam().getId());
    }
}
