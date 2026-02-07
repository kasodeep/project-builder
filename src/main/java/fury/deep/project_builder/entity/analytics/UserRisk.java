package fury.deep.project_builder.entity.analytics;

public record UserRisk(
        String userId,
        String username,
        int blockingTasks,
        int blockedUsers,
        int riskScore
) {
}