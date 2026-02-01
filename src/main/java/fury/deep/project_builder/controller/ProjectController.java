package fury.deep.project_builder.controller;

import fury.deep.project_builder.dto.project.ProjectCreateRequest;
import fury.deep.project_builder.dto.project.ProjectUpdateRequest;
import fury.deep.project_builder.entity.project.Project;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects(AuthContextHolder.getUser()));
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createProject(@Valid @RequestBody ProjectCreateRequest projectCreateRequest) {
        projectService.createProject(projectCreateRequest, AuthContextHolder.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/update")
    public ResponseEntity<Project> updateProject(@Valid @RequestBody ProjectUpdateRequest projectUpdateRequest) {
        projectService.updateProject(projectUpdateRequest, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }
}
