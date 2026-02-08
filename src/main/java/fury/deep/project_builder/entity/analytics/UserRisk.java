package fury.deep.project_builder.entity.analytics;

/**
 * A proper metric to reduce the dependency on a user, and distribute the project risk across different people.
 *
 * @author night_fury_44
 */
public record UserRisk(
        String userId,
        String username,
        int blockingTasks,
        int blockedUsers,
        int riskScore
) {
}