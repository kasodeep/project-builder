package fury.deep.project_builder.service;

import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.entity.team.Team;
import fury.deep.project_builder.exception.ResourceNotFoundException;
import fury.deep.project_builder.repository.TeamMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamMapper teamMapper;

    public TeamService(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    public List<Team> findAllTeams() {
        return teamMapper.findAll();
    }

    public boolean existsById(String id) {
        if (!teamMapper.existsById(id)) {
            throw new ResourceNotFoundException(ErrorMessages
                    .TEAM_NOT_FOUND
                    .formatted(id));
        }
        return true;
    }
}
