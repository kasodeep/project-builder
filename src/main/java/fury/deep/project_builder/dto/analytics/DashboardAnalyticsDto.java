package fury.deep.project_builder.dto.analytics;

public record DashboardAnalyticsDto(
        ProjectHealthDto health,
        ProjectFlowDto flow,
        DependencyRiskDto dependency,
        TeamCapacityDto team
) {}
