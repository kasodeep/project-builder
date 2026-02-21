package fury.deep.project_builder.service;

import fury.deep.project_builder.dto.user.UserDto;
import fury.deep.project_builder.dto.user.UserLoginRequest;
import fury.deep.project_builder.dto.user.UserRegisterRequest;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.exception.UnAuthorizedException;
import fury.deep.project_builder.repository.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User service to manage the data related to User entity.
 *
 * @author night_fury_44
 */
@Service
public class UserService {

    private final TeamService teamService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(TeamService teamService, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.teamService = teamService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * It registers the user to the db, by validating the team, and the username.
     *
     */
    public UserDto registerUser(UserRegisterRequest userRegisterRequest) {
        String teamId = userRegisterRequest.teamId();
        teamService.existsById(teamId);

        User user = fromUserRegisterRequest(userRegisterRequest);
        userMapper.registerUser(user, teamId);

        return fromUser(user);
    }

    /**
     * For UI client to validate a user, and authenticate him/her.
     *
     */
    public UserDto loginUser(UserLoginRequest userLoginRequest) {
        User user = userMapper.findByUsername(userLoginRequest.username());
        if (user == null || !passwordEncoder.matches(userLoginRequest.password(), user.getPassword())) {
            throw new UnAuthorizedException("User not authorized");
        }

        return fromUser(user);
    }

    /**
     * The method convert the request to a user, by mapping.
     * It encodes the password for security.
     */
    private User fromUserRegisterRequest(UserRegisterRequest userRegisterRequest) {
        return User.builder()
                .username(userRegisterRequest.username())
                .email(userRegisterRequest.email())
                .password(passwordEncoder.encode(userRegisterRequest.password()))
                .role(userRegisterRequest.role())
                .build();
    }

    private UserDto fromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .teamId(user.getTeamId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    /**
     * The method returns all the users, belonging to the team with teamId.
     */
    public List<UserDto> getUsersByTeamId(String teamId) {
        List<User> users = userMapper.findByTeamId(teamId);

        if (users == null || users.isEmpty()) {
            return List.of();
        }

        return users.stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .build())
                .toList();
    }
}
