package fury.deep.project_builder.service;

import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.entity.team.Team;
import fury.deep.project_builder.exception.ResourceNotFoundException;
import fury.deep.project_builder.repository.TeamMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * List of services related to the team, which binds the project.
 *
 * @author night_fury_44
 */
@Service
public class TeamService {

    private final TeamMapper teamMapper;

    public TeamService(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    /**
     * The method returns all the team that are present.
     *
     */
    public List<Team> findAllTeams() {
        return teamMapper.findAll();
    }

    /**
     * The method try to find the team by the given id.
     *
     */
    public Team findById(String id) {
        Team team = teamMapper.findById(id);

        if (team == null) throw new ResourceNotFoundException(ErrorMessages.TEAM_NOT_FOUND.formatted(id));
        return team;
    }

    /**
     * Validation to check if the given team exists on basis of id.
     *
     */
    public void existsById(String id) {
        if (!teamMapper.existsById(id)) {
            throw new ResourceNotFoundException(ErrorMessages
                    .TEAM_NOT_FOUND
                    .formatted(id));
        }
    }
}
