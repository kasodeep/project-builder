package fury.deep.project_builder.dto.project.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request for updating the managers responsible to manage the project.
 *
 * @author - deep
 */
public record AddProjectManagersRequest(
        @NotBlank String projectId,
        @NotEmpty(message = "Managers list cannot be empty")
        List<@NotBlank(message = "Manager username cannot be blank") String> managers
) {
}
