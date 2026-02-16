package fury.deep.project_builder.controller;

import fury.deep.project_builder.entity.team.Team;
import fury.deep.project_builder.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Team>> findAllTeams() {
        return ResponseEntity.ok(teamService.findAllTeams());
    }
}
