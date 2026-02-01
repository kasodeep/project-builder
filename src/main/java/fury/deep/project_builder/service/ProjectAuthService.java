package fury.deep.project_builder.service;

import fury.deep.project_builder.dto.project.auth.AddProjectManagerResponse;
import fury.deep.project_builder.dto.project.auth.AddProjectManagersRequest;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.repository.ProjectAuthMapper;
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

    public ProjectAuthService(ProjectService projectService, ProjectAuthMapper projectAuthMapper) {
        this.projectService = projectService;
        this.projectAuthMapper = projectAuthMapper;
    }

    /**
     * The method updates the managers by validating the access to project.
     *
     * @throws IllegalArgumentException - When the list of managers contains the owner.
     *
     */
    @Transactional
    public AddProjectManagerResponse addManagers(AddProjectManagersRequest request, User user) {
        projectService.ValidateAccess(request.projectId(), user);

        if (request.managers().contains(user.getUsername())) {
            throw new IllegalArgumentException("Owner cannot be added as manager");
        }

        projectAuthMapper.replaceManagers(request.projectId(), request.managers(), user.getTeam().getId());
        return new AddProjectManagerResponse(request.projectId(), request.managers());
    }

}
