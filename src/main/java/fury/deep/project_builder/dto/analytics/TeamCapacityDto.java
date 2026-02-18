package fury.deep.project_builder.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamCapacityDto {

    private int activeProjects;
    private int activeTasks;
    private int avgTasksPerUser;
    private int overloadedUsers;
    private int avgCompletionTimeDays;
    private int burnoutRiskScore;
}
