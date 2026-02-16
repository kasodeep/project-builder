package fury.deep.project_builder.controller.task;

import fury.deep.project_builder.dto.task.CreateTaskRequest;
import fury.deep.project_builder.dto.task.UpdateTaskRequest;
import fury.deep.project_builder.entity.task.Task;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Task", description = "Task lifecycle and management APIs")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/v1/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Create a new task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or feature not found")
    })
    @PostMapping("/create")
    public ResponseEntity<Void> createTask(@Valid @RequestBody CreateTaskRequest request) {
        taskService.createTask(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update existing task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/update")
    public ResponseEntity<Void> updateTask(@Valid @RequestBody UpdateTaskRequest request) {
        taskService.updateTask(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        taskService.deleteTask(taskId, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@Parameter(description = "Task ID") @PathVariable String taskId) {
        Task task = taskService.findById(taskId, AuthContextHolder.getUser());
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Get all tasks for a project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProjectId(@PathVariable String projectId) {
        List<Task> tasks = taskService.findTasksByProjectId(projectId, AuthContextHolder.getUser());
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Get tasks assigned to authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user")
    public ResponseEntity<List<Task>> getTasksForUser() {
        List<Task> tasks = taskService.tasksForUser(AuthContextHolder.getUser());
        return ResponseEntity.ok(tasks);
    }
}
