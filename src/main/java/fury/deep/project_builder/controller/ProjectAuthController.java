package fury.deep.project_builder.controller;

import fury.deep.project_builder.dto.project.auth.AddProjectManagerResponse;
import fury.deep.project_builder.dto.project.auth.AddProjectManagersRequest;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.ProjectAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/project-auth")
public class ProjectAuthController {

    private final ProjectAuthService projectAuthService;

    public ProjectAuthController(ProjectAuthService projectAuthService) {
        this.projectAuthService = projectAuthService;
    }

    @PostMapping("/add")
    public ResponseEntity<AddProjectManagerResponse> addManagers(@Validated @RequestBody AddProjectManagersRequest request) {
        return new ResponseEntity<>(projectAuthService.addManagers(request, AuthContextHolder.getUser()), HttpStatus.OK);
    }
}
