package fury.deep.project_builder.dto.analytics;

public record FeatureAnalyticsDto(
        String featureId,
        String featureName,
        int totalTasks,
        int completedTasks,
        int blockedTasks,
        double completionRatio,
        boolean atRisk
) {
}

