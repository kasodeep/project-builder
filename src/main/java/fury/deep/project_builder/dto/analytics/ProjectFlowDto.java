package fury.deep.project_builder.dto.analytics;

public record ProjectFlowDto(
        int wipCount,
        int throughput7d,
        int throughput30d,
        int avgCycleTime,
        int reopenedTasks
) {}
