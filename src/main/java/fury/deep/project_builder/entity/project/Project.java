package fury.deep.project_builder.entity.project;

import fury.deep.project_builder.entity.team.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Project is a temporary endeavor with a beginning and an end and must be used to create a unique product, service.
 *
 * @author night_fury_44
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private String id;
    private String name;
    private Team team;
    private String owner;
    private List<String> managers;
    private Integer progress;
    private LocalDate start;
    private LocalDate end;
    private String updatedBy;
    private Instant updatedAt;
}
