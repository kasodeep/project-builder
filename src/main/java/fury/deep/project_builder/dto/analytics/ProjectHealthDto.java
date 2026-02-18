package fury.deep.project_builder.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectHealthDto {
    private int healthScore;
    private int overdueTasks;
    private int blockedTasks;
    private String riskLevel;
}
