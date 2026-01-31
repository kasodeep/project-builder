package fury.deep.project_builder.service;

import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.dto.project.auth.AddProjectManagerResponse;
import fury.deep.project_builder.dto.project.auth.AddProjectManagersRequest;
import fury.deep.project_builder.entity.project.Project;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.repository.ProjectAuthMapper;
import fury.deep.project_builder.repository.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ProjectAuthService {

    private final ProjectService projectService;
    private final UserMapper userMapper;
    private final ProjectAuthMapper projectAuthMapper;

    public ProjectAuthService(ProjectService projectService, UserMapper userMapper, ProjectAuthMapper projectAuthMapper) {
        this.projectService = projectService;
        this.userMapper = userMapper;
        this.projectAuthMapper = projectAuthMapper;
    }

    /*
     * 1. The user must be owner or manager of the project.
     * */
    @Transactional
    public AddProjectManagerResponse addManagers(
            AddProjectManagersRequest request,
            User user
    ) {
        Project project = projectService.findById(request.projectId());
        projectService.UserValidation(project, user);

        if (request.managers().contains(project.getOwner())) {
            throw new IllegalArgumentException("Owner cannot be added as manager");
        }

        Set<String> unique = Set.copyOf(request.managers());
        if (unique.size() != request.managers().size()) {
            throw new IllegalArgumentException("Duplicate managers not allowed");
        }

        int count = userMapper.countUsersInTeam(request.managers(), project.getTeam().getId());

        // TODO: Exception manager
        if (count != request.managers().size()) {
            throw new IllegalArgumentException("All managers must exist and belong to the project team");
        }

        projectAuthMapper.replaceManagers(project.getId(), request.managers(), project.getTeam().getId());
        return new AddProjectManagerResponse(project.getId(), request.managers());
    }
}
