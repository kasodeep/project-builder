package fury.deep.project_builder.entity.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * A task represents a unit quantity of work done on a project by group of people (assignees).
 * It must belong to a project and categorized by feature.
 * The priority metric is used to perform weighted analytics on the project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String id;
    private String projectId;
    private List<String> assignees;
    private String name;
    private Feature feature;
    private List<String> dependencies;
    private Integer priority;
    private Status status;
    private LocalDate start;
    private LocalDate end;
    private String updatedBy;
    private Instant updatedAt;
    private Instant startedAt;
    private Instant completedAt;
}
