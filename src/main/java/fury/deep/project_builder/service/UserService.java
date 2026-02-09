package fury.deep.project_builder.service;

import fury.deep.project_builder.dto.user.UserLoginRequest;
import fury.deep.project_builder.dto.user.UserRegisterRequest;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.exception.UnAuthorizedException;
import fury.deep.project_builder.repository.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * User service to manage the data related to User entity.
 *
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
    public void registerUser(UserRegisterRequest userRegisterRequest) {
        String teamId = userRegisterRequest.teamId();
        teamService.existsById(teamId);

        // TODO: username taken error.

        User user = fromUserRegisterRequest(userRegisterRequest);
        userMapper.registerUser(user, teamId);
    }

    public void loginUser(UserLoginRequest userLoginRequest) {
        User user = userMapper.findByUsername(userLoginRequest.username());
        if (user == null || !passwordEncoder.matches(userLoginRequest.password(), user.getPassword())) {
            throw new UnAuthorizedException("User not authorized");
        }
    }

    /**
     * The method convert the request to a user, by mapping.
     * It encodes the password for security.
     *
     */
    private User fromUserRegisterRequest(UserRegisterRequest userRegisterRequest) {
        return User.builder()
                .username(userRegisterRequest.username())
                .email(userRegisterRequest.email())
                .password(passwordEncoder.encode(userRegisterRequest.password()))
                .role(userRegisterRequest.role())
                .build();
    }
}
