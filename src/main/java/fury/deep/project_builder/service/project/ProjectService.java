package fury.deep.project_builder.service.project;

import fury.deep.project_builder.config.CacheConfig;
import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.dto.project.ProjectCreateRequest;
import fury.deep.project_builder.dto.project.ProjectUpdateRequest;
import fury.deep.project_builder.entity.project.Project;
import fury.deep.project_builder.entity.user.Role;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.exception.OptimisticLockException;
import fury.deep.project_builder.exception.UnAuthorizedException;
import fury.deep.project_builder.repository.project.ProjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides methods related to managing the project entity from creating, to update, to findAll.
 *
 * @author night_fury_44
 */
@Slf4j
@Service
public class ProjectService {

    private static final int INITIAL_PROJECT_PROGRESS = 0;

    private final ProjectMapper projectMapper;

    // --- Metrics ---
    private final Counter projectCreatedCounter;
    private final Counter projectUpdateConflictCounter;
    private final Timer projectFetchTimer;

    public ProjectService(ProjectMapper projectMapper, MeterRegistry meterRegistry) {
        this.projectMapper = projectMapper;

        this.projectCreatedCounter = Counter.builder("project.created.total")
                .description("Total number of projects created")
                .register(meterRegistry);

        this.projectUpdateConflictCounter = Counter.builder("project.update.conflict.total")
                .description("Number of optimistic lock conflicts during project update")
                .register(meterRegistry);

        this.projectFetchTimer = Timer.builder("project.fetch.duration")
                .description("Time taken to fetch and count projects from the database")
                .register(meterRegistry);
    }

    /**
     * The project can be created by MANAGER, and the team will be the one the user belongs to.
     */
    @CacheEvict(value = CacheConfig.PROJECTS_BY_TEAM, key = "#user.teamId")
    @Observed(name = "project.create", contextualName = "createProject")
    public void createProject(ProjectCreateRequest projectCreateRequest, User user) {
        if (user.getRole() != Role.MANAGER) {
            log.warn("Unauthorized project creation attempt user={} role={}",
                    user.getUsername(), user.getRole());
            throw new UnAuthorizedException(ErrorMessages.UNAUTHORIZED_ACTION);
        }

        Project project = fromProjectCreateRequest(projectCreateRequest, user);
        projectMapper.insertProject(project, user.getTeamId());

        projectCreatedCounter.increment();
        log.info("Project created name={} owner={} teamId={}",
                project.getName(), user.getUsername(), user.getTeamId());
    }

    /**
     * Validates access then performs an optimistic-lock-aware update.
     *
     * @throws OptimisticLockException when {@code request.version()} doesn't match the DB row,
     *                                 meaning another request modified the project concurrently.
     */
    @CacheEvict(value = CacheConfig.PROJECTS_BY_TEAM, key = "#user.teamId")
    @Observed(name = "project.update", contextualName = "updateProject")
    public void updateProject(ProjectUpdateRequest request, User user) {
        validateAccess(request.projectId(), user);

        int rowsUpdated = projectMapper.updateProjectMetadata(request, user.getUsername());

        if (rowsUpdated == 0) {
            projectUpdateConflictCounter.increment();
            log.warn("Optimistic lock conflict projectId={} suppliedVersion={} user={}",
                    request.projectId(), request.version(), user.getUsername());
            throw new OptimisticLockException(
                    "Project was modified by another request. Please refresh and retry.");
        }

        log.info("Project updated projectId={} user={}", request.projectId(), user.getUsername());
    }

    /**
     * Returns a list of projects for the user's team.
     *
     */
    @Cacheable(value = CacheConfig.PROJECTS_BY_TEAM, key = "#user.teamId")
    @Observed(name = "project.findAll", contextualName = "getAllProjects")
    public List<Project> getAllProjects(User user) {
        return projectFetchTimer.record(() -> {
            List<Project> items = projectMapper.findAll(user.getTeamId());

            log.debug("Projects fetched teamId={} total={}",
                    user.getTeamId(), items.size());

            return items;
        });
    }

    /**
     * Checks the access by checking if the user is owner or manager.
     */
    public void validateAccess(String projectId, User user) {
        boolean allowed = projectMapper.isOwnerOrManager(projectId, user.getUsername());
        if (!allowed) {
            log.warn("Access denied projectId={} user={}", projectId, user.getUsername());
            throw new UnAuthorizedException(ErrorMessages.UNAUTHORIZED_ACTION);
        }
    }

    private Project fromProjectCreateRequest(ProjectCreateRequest projectCreateRequest, User user) {
        return Project.builder()
                .name(projectCreateRequest.name())
                .owner(user.getUsername())
                .progress(INITIAL_PROJECT_PROGRESS)
                .start(projectCreateRequest.start())
                .end(projectCreateRequest.end())
                .updatedBy(user.getUsername())
                .build();
    }
}