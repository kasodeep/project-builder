package fury.deep.project_builder.dto.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to log in a user to the service.
 *
 * @author night_fury_44
 */
public record UserLoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
