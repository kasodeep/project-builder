package fury.deep.project_builder.dto.user;

import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(
        @NotNull String username,
        @NotNull String password
) {
}
