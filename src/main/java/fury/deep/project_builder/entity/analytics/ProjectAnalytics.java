package fury.deep.project_builder.entity.analytics;

/**
 * Provides a general overview over the project with tasks being completed or blocked.
 *
 */
public record ProjectAnalytics(
        int totalTasks,
        int completedTasks,
        int blockedTasks
) {
}

