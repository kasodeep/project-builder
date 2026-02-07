package fury.deep.project_builder.service.task;

import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.dto.task.CreateTaskRequest;
import fury.deep.project_builder.entity.task.Feature;
import fury.deep.project_builder.entity.task.Task;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.events.TaskCreatedEvent;
import fury.deep.project_builder.exception.ResourceNotFoundException;
import fury.deep.project_builder.repository.task.TaskMapper;
import fury.deep.project_builder.service.FeatureService;
import fury.deep.project_builder.service.project.ProjectService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TaskService {

    private final TaskMapper taskMapper;
    private final ProjectService projectService;
    private final FeatureService featureService;
    private final ApplicationEventPublisher publisher;

    public TaskService(TaskMapper taskMapper, ProjectService projectService, FeatureService featureService, ApplicationEventPublisher publisher) {
        this.taskMapper = taskMapper;
        this.projectService = projectService;
        this.featureService = featureService;
        this.publisher = publisher;
    }

    /**
     *
     */
    @Transactional
    public void createTask(CreateTaskRequest createTaskRequest, User user) {
        projectService.validateAccess(createTaskRequest.projectId(), user);
        Feature feature = featureService.findById(createTaskRequest.featureId());

        Task task = fromCreateTaskRequest(createTaskRequest, user, feature);
        taskMapper.insertTask(task);

        publisher.publishEvent(
                new TaskCreatedEvent(
                        task.getId(),
                        task.getProjectId(),
                        task.getStatus()
                )
        );
    }

    /**
     * Method validates and finds the task for the user.
     * Suppose we want the task without validation, which will be a rare use-case.
     * But, every findById or find methods with validation should work.
     *
     */
    public Task findById(String taskId, User user) {
        Task taskById = taskMapper.findTaskById(taskId);
        if (taskById == null) throw new ResourceNotFoundException(ErrorMessages.TASK_NOT_FOUND.formatted(taskId));

        projectService.validateAccess(taskById.getProjectId(), user);
        return taskById;
    }

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

    private Task fromCreateTaskRequest(CreateTaskRequest createTaskRequest, User user, Feature feature) {
        return Task.builder()
                .name(createTaskRequest.name())
                .projectId(createTaskRequest.projectId())
                .feature(feature)
                .priority(createTaskRequest.priority())
                .status(createTaskRequest.status())
                .start(createTaskRequest.start())
                .end(createTaskRequest.end())
                .updatedAt(Instant.now())
                .updatedBy(user.getUsername())
                .build();
    }
}
