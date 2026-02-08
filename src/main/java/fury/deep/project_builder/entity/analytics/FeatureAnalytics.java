package fury.deep.project_builder.entity.analytics;

/**
 * Overview on the subset of a project area -> feature.
 *
 * @author night_fury_44
 *
 */
public record FeatureAnalytics(
        String featureId,
        String featureName,
        int totalTasks,
        int completedTasks,
        int blockedTasks
) {
}

