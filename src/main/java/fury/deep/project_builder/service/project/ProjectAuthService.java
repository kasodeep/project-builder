package fury.deep.project_builder.service.project;

import fury.deep.project_builder.dto.project.auth.AddProjectManagersRequest;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.repository.UserMapper;
import fury.deep.project_builder.repository.project.ProjectAuthMapper;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to manage the access to a project by updating the managers.
 *
 * @author night_fury_44
 */
@Slf4j
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
     * Replaces all managers of a project after validating:
     * <ol>
     *   <li>The caller is the project owner or current manager.</li>
     *   <li>The owner is not adding themselves as a manager.</li>
     *   <li>All supplied usernames belong to the same team.</li>
     * </ol>
     *
     * @throws IllegalArgumentException when the owner is in the managers list
     *                                  or when a username belongs to a different team
     */
    @Transactional
    @Observed(name = "project.addManagers", contextualName = "addManagers")
    public void addManagers(AddProjectManagersRequest request, User user) {
        projectService.validateAccess(request.projectId(), user);

        if (request.managers().contains(user.getUsername())) {
            log.warn("Owner attempted to add themselves as manager projectId={} user={}",
                    request.projectId(), user.getUsername());
            throw new IllegalArgumentException("Owner cannot be added as manager");
        }

        int validCount = userMapper.countUsersInTeam(request.managers(), user.getTeamId());
        if (validCount != request.managers().size()) {
            log.warn("Manager team mismatch projectId={} requested={} validInTeam={}",
                    request.projectId(), request.managers().size(), validCount);
            throw new IllegalArgumentException(
                    "All managers must belong to the same team as the project");
        }

        projectAuthMapper.replaceManagers(request.projectId(), request.managers(), user.getTeamId());

        log.info("Managers replaced projectId={} count={} by={}",
                request.projectId(), request.managers().size(), user.getUsername());
    }
}