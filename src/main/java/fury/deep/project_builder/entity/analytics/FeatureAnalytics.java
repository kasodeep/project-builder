package fury.deep.project_builder.entity.analytics;

public record FeatureAnalytics(
        String featureId,
        String featureName,
        int totalTasks,
        int completedTasks,
        int blockedTasks
) {
}

