package fury.deep.project_builder.entity.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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
}
