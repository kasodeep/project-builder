package fury.deep.project_builder.dto.user;

import fury.deep.project_builder.entity.user.Role;
import lombok.Builder;

@Builder
public record UserDto(
        String id,
        String username,
        Role role,
        String teamId
) {
}
