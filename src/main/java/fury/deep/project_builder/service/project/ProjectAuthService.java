package fury.deep.project_builder.service.project;

import fury.deep.project_builder.dto.project.auth.AddProjectManagersRequest;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.repository.UserMapper;
import fury.deep.project_builder.repository.project.ProjectAuthMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to manage the access to a project by updating the managers.
 *
 * @author night_fury_44
 */
@Service
public class ProjectAuthService {

    private final ProjectService projectService;
    private final ProjectAuthMapper projectAuthMapper;
    private final UserMapper userMapper;

    public ProjectAuthService(ProjectService projectService,
                              ProjectAuthMapper projectAuthMapper,
                              UserMapper userMapper) {
        this.projectService = projectService;
        this.projectAuthMapper = projectAuthMapper;
        this.userMapper = userMapper;
    }

    /**
     * The method updates the managers by validating the access to project.
     *
     * @throws IllegalArgumentException - When the list of managers contains the owner.
     *
     */
    @Transactional
    public void addManagers(AddProjectManagersRequest request, User user) {
        projectService.validateAccess(request.projectId(), user);

        // Owner cannot add himself
        if (request.managers().contains(user.getUsername())) {
            throw new IllegalArgumentException("Owner cannot be added as manager");
        }

        // Validate all users belong to same team
        int validCount = userMapper.countUsersInTeam(request.managers(), user.getTeam().getId());
        if (validCount != request.managers().size()) {
            throw new IllegalArgumentException(
                    "All managers must belong to the same team as the project"
            );
        }

        projectAuthMapper.replaceManagers(request.projectId(), request.managers(), user.getTeam().getId());
    }
}

