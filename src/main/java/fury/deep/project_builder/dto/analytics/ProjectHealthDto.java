package fury.deep.project_builder.dto.analytics;

public record ProjectHealthDto(
        int healthScore,
        int scheduleVarianceDays,
        int progressVariancePct,
        int overdueTasks,
        int blockedTasks,
        String riskLevel
) {}
