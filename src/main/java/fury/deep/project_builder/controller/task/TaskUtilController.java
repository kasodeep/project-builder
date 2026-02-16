package fury.deep.project_builder.controller.task;

import fury.deep.project_builder.dto.task.util.AddAssigneeRequest;
import fury.deep.project_builder.dto.task.util.AddDependenciesRequest;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.task.TaskUtilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Task Utilities", description = "Task dependency and assignee management")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/v1/task-util")
public class TaskUtilController {

    private final TaskUtilService taskUtilService;

    public TaskUtilController(TaskUtilService taskUtilService) {
        this.taskUtilService = taskUtilService;
    }

    @Operation(summary = "Replace task assignees")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assignees updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid team membership"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/add-assignees")
    public ResponseEntity<Void> addAssignees(@Valid @RequestBody AddAssigneeRequest request) {
        taskUtilService.addAssignees(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Replace task dependencies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dependencies updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid project scope"),
            @ApiResponse(responseCode = "409", description = "Dependency cycle detected"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/add-dependencies")
    public ResponseEntity<Void> addDependencies(@Valid @RequestBody AddDependenciesRequest request) {
        taskUtilService.addDependencies(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }
}
