package fury.deep.project_builder.service;

import fury.deep.project_builder.dto.user.UserRegisterRequest;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.repository.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // TODO: Username taken error, plus use the error syntax.
    public void registerUser(UserRegisterRequest userRegisterRequest) {
        String teamId = userRegisterRequest.teamId();
        teamService.existsById(teamId);

        User user = fromUserRegisterRequest(userRegisterRequest);
        userMapper.registerUser(user, teamId);
    }

    private User fromUserRegisterRequest(UserRegisterRequest userRegisterRequest) {
        return User.builder()
                .username(userRegisterRequest.username())
                .email(userRegisterRequest.email())
                .password(passwordEncoder.encode(userRegisterRequest.password()))
                .role(userRegisterRequest.role())
                .build();
    }
}
