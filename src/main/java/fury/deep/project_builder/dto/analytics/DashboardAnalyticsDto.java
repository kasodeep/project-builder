package fury.deep.project_builder.dto.analytics;

import java.util.List;

public record DashboardAnalyticsDto(
        ProjectAnalyticsDto project,
        List<FeatureAnalyticsDto> features,
        List<UserDependencyRiskDto> users
) {
}

