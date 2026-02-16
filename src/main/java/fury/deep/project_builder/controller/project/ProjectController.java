package fury.deep.project_builder.controller.project;

import fury.deep.project_builder.dto.project.ProjectCreateRequest;
import fury.deep.project_builder.dto.project.ProjectUpdateRequest;
import fury.deep.project_builder.entity.project.Project;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project", description = "Project lifecycle and metadata management APIs")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/v1/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Get all projects for authenticated user's team")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects(AuthContextHolder.getUser()));
    }

    @Operation(summary = "Create new project (Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "User not authorized")
    })
    @PostMapping("/create")
    public ResponseEntity<Void> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        projectService.createProject(request, AuthContextHolder.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Update project metadata")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "User not authorized")
    })
    @PatchMapping("/update")
    public ResponseEntity<Void> updateProject(@Valid @RequestBody ProjectUpdateRequest request) {
        projectService.updateProject(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }
}
