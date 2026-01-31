package fury.deep.project_builder.service;

import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.dto.project.ProjectCreateRequest;
import fury.deep.project_builder.dto.project.ProjectUpdateRequest;
import fury.deep.project_builder.entity.project.Project;
import fury.deep.project_builder.entity.user.Role;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.exception.ResourceNotFoundException;
import fury.deep.project_builder.exception.UnAuthorizedException;
import fury.deep.project_builder.repository.ProjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final Integer INITIAL_PROJECT_PROGRESS = 0;

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    /*
     * 1. The principal should be a manager. We allow project with same names.
     * 2. We create the project, and perform the validation.
     * 3. Managers can be added separately for authorization. The list will be fetched during data.
     * */
    public void createProject(ProjectCreateRequest projectCreateRequest, User user) {
        if (user.getRole() != Role.MANAGER) {
            throw new UnAuthorizedException(ErrorMessages.UNAUTHORIZED_ACTION);
        }

        Project project = fromProjectCreateRequest(projectCreateRequest, user);
        projectMapper.insertProject(project);
    }

    /*
     * 1. the updater should be the owner or the manager of the project.
     * */
    public void updateProject(ProjectUpdateRequest projectUpdateRequest, User user) {
        Project project = findById(projectUpdateRequest.projectId());
        UserValidation(project, user);

        projectMapper.updateProjectMetadata(projectUpdateRequest, user.getUsername());
    }

    public List<Project> getAllProjects(User user) {
        return projectMapper.findAll(user.getTeam().getId());
    }

    public Project findById(String projectId) {
        Project project = projectMapper.findById(projectId);

        if (project == null) {
            throw new ResourceNotFoundException(ErrorMessages.PROJECT_NOT_FOUND.formatted(projectId));
        }
        return project;
    }

    public void UserValidation(Project project, User user) {
        if (!project.getOwner().equals(user.getUsername())
                && !project.getManagers().contains(user.getUsername())) {
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
