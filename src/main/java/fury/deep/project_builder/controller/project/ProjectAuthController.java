package fury.deep.project_builder.controller.project;

import fury.deep.project_builder.dto.project.auth.AddProjectManagersRequest;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.project.ProjectAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Project Authorization", description = "Project manager access control APIs")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/v1/project-auth")
public class ProjectAuthController {

    private final ProjectAuthService projectAuthService;

    public ProjectAuthController(ProjectAuthService projectAuthService) {
        this.projectAuthService = projectAuthService;
    }

    @Operation(summary = "Replace project managers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Managers updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid manager list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PostMapping("/add")
    public ResponseEntity<Void> addManagers(@Valid @RequestBody AddProjectManagersRequest request) {
        projectAuthService.addManagers(request, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }
}
