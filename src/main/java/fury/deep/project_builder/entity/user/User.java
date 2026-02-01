package fury.deep.project_builder.entity.user;

import fury.deep.project_builder.entity.team.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User is the basic client who access the services, after being authenticated and authorized.
 *
 * @author night_fury_44
 */
@Data
@Builder
@NoArgsConstructor // We need both for builder to work and mybatis to map.
@AllArgsConstructor
public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private Team team;
}
