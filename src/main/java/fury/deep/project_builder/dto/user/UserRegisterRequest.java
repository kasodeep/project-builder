package fury.deep.project_builder.dto.user;

import fury.deep.project_builder.entity.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to register a user to the service, with a unique username and to an existing team.
 *
 * @author night_fury_44
 */
public record UserRegisterRequest(
        @NotBlank String username,
        @Email String email,
        @NotBlank String password,
        Role role,
        @NotBlank String teamId
) {
}
