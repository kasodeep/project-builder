package fury.deep.project_builder.dto.analytics;

public record TeamCapacityDto(
        int activeProjects,
        int activeTasks,
        int avgTasksPerUser,
        int overloadedUsers,
        int avgCompletionTimeDays,
        int burnoutRiskScore
) {}
