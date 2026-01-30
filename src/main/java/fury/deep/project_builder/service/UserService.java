package fury.deep.project_builder.service;

import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.dto.user.UserRegisterRequest;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.exception.ResourceNotFoundException;
import fury.deep.project_builder.repository.TeamMapper;
import fury.deep.project_builder.repository.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final TeamMapper teamMapper;
    private final UserMapper userMapper;

    public UserService(TeamMapper teamMapper, UserMapper userMapper) {
        this.teamMapper = teamMapper;
        this.userMapper = userMapper;
    }

    // TODO: Username taken error, plus use the error syntax.
    public void registerUser(UserRegisterRequest userRegisterRequest) {
        String teamId = userRegisterRequest.teamId();
        if (!teamMapper.existsById(teamId)) {
            throw new ResourceNotFoundException(ErrorMessages
                    .TEAM_NOT_FOUND
                    .formatted(teamId));
        }

        User user = fromUserRegisterDTO(userRegisterRequest);
        userMapper.registerUser(user, teamId);
    }

    private User fromUserRegisterDTO(UserRegisterRequest userRegisterRequest) {
        return User.builder()
                .username(userRegisterRequest.username())
                .email(userRegisterRequest.email())
                .password(userRegisterRequest.password())
                .role(userRegisterRequest.role())
                .build();
    }
}
