package fury.deep.project_builder.dto.analytics;

public record ProjectHealthDto(
        int healthScore,
        int overdueTasks,
        int blockedTasks,
        String riskLevel
) {}
