package fury.deep.project_builder.entity.analytics;

public record ProjectAnalytics(
        int totalTasks,
        int completedTasks,
        int blockedTasks
) {
}

