package fury.deep.project_builder.dto.user;

import fury.deep.project_builder.entity.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserRegisterRequest(
        @NotNull String username,
        @Email String email,
        @NotNull String password,
        @NotNull Role role,
        @NotNull String teamId
) {
}
