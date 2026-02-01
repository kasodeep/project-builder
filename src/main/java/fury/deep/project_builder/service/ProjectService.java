package fury.deep.project_builder.service;

import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.dto.project.ProjectCreateRequest;
import fury.deep.project_builder.dto.project.ProjectUpdateRequest;
import fury.deep.project_builder.entity.project.Project;
import fury.deep.project_builder.entity.user.Role;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.exception.UnAuthorizedException;
import fury.deep.project_builder.repository.ProjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides methods related to managing the project entity from creating, to update, to findAll.
 *
 * @author night_fury_44
 *
 */
@Service
public class ProjectService {

    private final Integer INITIAL_PROJECT_PROGRESS = 0;

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    /**
     * The project can be created by MANAGER, and the team will be the one the user belongs to.
     *
     */
    public void createProject(ProjectCreateRequest projectCreateRequest, User user) {
        if (user.getRole() != Role.MANAGER) {
            throw new UnAuthorizedException(ErrorMessages.UNAUTHORIZED_ACTION);
        }

        Project project = fromProjectCreateRequest(projectCreateRequest, user);
        projectMapper.insertProject(project);
    }

    /**
     * Method validates the access to the project and update the details accordingly.
     *
     */
    public void updateProject(ProjectUpdateRequest request, User user) {
        ValidateAccess(request.projectId(), user);
        projectMapper.updateProjectMetadata(request, user.getUsername());
    }

    /**
     * The method finds all the projects, queried by the team of the user.
     */
    public List<Project> getAllProjects(User user) {
        return projectMapper.findAll(user.getTeam().getId());
    }

    /**
     * Checks the access by checking if the user is owner or manager.
     *
     */
    public void ValidateAccess(String projectId, User user) {
        boolean allowed = projectMapper.isOwnerOrManager(projectId, user.getUsername());

        if (!allowed) {
            throw new UnAuthorizedException(ErrorMessages.UNAUTHORIZED_ACTION);
        }
    }

    private Project fromProjectCreateRequest(ProjectCreateRequest projectCreateRequest, User user) {
        return Project.builder()
                .name(projectCreateRequest.name())
                .team(user.getTeam())
                .owner(user.getUsername())
                .progress(INITIAL_PROJECT_PROGRESS)
                .start(projectCreateRequest.start())
                .end(projectCreateRequest.end())
                .updatedBy(user.getUsername())
                .build();
    }
}
