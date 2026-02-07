package fury.deep.project_builder.dto.analytics;

public record UserDependencyRiskDto(
        String userId,
        String username,
        int blockingTasks,
        int blockedUsers,
        int riskScore
) {
}

