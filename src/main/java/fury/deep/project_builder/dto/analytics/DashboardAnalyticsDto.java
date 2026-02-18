package fury.deep.project_builder.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardAnalyticsDto {
    private ProjectHealthDto health;
    private ProjectFlowDto flow;
    private DependencyRiskDto dependency;
    private TeamCapacityDto team;
}
