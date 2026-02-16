package fury.deep.project_builder.controller.task;

import fury.deep.project_builder.dto.task.CreateTaskRequest;
import fury.deep.project_builder.dto.task.UpdateTaskRequest;
import fury.deep.project_builder.entity.task.Task;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.task.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createTask(@Valid @RequestBody CreateTaskRequest request) {
        taskService.createTask(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateTask(@Valid @RequestBody UpdateTaskRequest request) {
        taskService.updateTask(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        taskService.deleteTask(taskId, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable String taskId) {
        Task task = taskService.findById(taskId, AuthContextHolder.getUser());
        return ResponseEntity.ok(task);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProjectId(@PathVariable String projectId) {
        List<Task> tasks = taskService.findTasksByProjectId(projectId, AuthContextHolder.getUser());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Task>> getTasksForUser() {
        List<Task> tasks = taskService.tasksForUser(AuthContextHolder.getUser());
        return ResponseEntity.ok(tasks);
    }
}
