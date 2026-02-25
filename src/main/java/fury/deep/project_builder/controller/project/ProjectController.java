package fury.deep.project_builder.controller.project;

import fury.deep.project_builder.dto.project.ProjectCreateRequest;
import fury.deep.project_builder.dto.project.ProjectUpdateRequest;
import fury.deep.project_builder.entity.project.Project;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.project.ProjectService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * GET /api/v1/project
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects(AuthContextHolder.getUser()));
    }

    @PostMapping
    public ResponseEntity<Void> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        projectService.createProject(request, AuthContextHolder.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * PATCH /api/v1/project
     */
    @PatchMapping
    public ResponseEntity<Void> updateProject(@Valid @RequestBody ProjectUpdateRequest request) {
        projectService.updateProject(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }
}