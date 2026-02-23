package fury.deep.project_builder.controller.project;

import fury.deep.project_builder.dto.project.auth.AddProjectManagersRequest;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.project.ProjectAuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/project-auth")
public class ProjectAuthController {

    private final ProjectAuthService projectAuthService;

    public ProjectAuthController(ProjectAuthService projectAuthService) {
        this.projectAuthService = projectAuthService;
    }

    @PostMapping
    public ResponseEntity<Void> addManagers(@Valid @RequestBody AddProjectManagersRequest request) {
        log.debug("POST /project-auth/add projectId={} user={}",
                request.projectId(), AuthContextHolder.getUser().getUsername());
        projectAuthService.addManagers(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }
}