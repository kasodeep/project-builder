package fury.deep.project_builder.service;

import fury.deep.project_builder.entity.team.Team;
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
}
