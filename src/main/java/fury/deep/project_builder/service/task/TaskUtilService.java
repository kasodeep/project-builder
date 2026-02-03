package fury.deep.project_builder.service.task;

import fury.deep.project_builder.dto.task.util.AddAssigneeRequest;
import fury.deep.project_builder.entity.task.Task;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.repository.UserMapper;
import fury.deep.project_builder.repository.task.TaskUtilMapper;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void addDependencies(String taskId, String projectId, List<String> dependencies) {
        taskUtilMapper.replaceDependencies(taskId, dependencies, projectId);
    }

    public void addAssignees(AddAssigneeRequest assigneeRequest, User user) {
        Task task = taskService.findById(assigneeRequest.taskId(), user);// also validates the user.

        int validCount = userMapper.countUsersInTeam(assigneeRequest.assignees(), user.getTeam().getId());
        if (validCount != assigneeRequest.assignees().size()) {
            throw new IllegalArgumentException(
                    "All managers must belong to the same team as the project"
            );
        }

        taskUtilMapper.replaceAssignees(task.getId(), assigneeRequest.assignees(), user.getTeam().getId());
    }
}
