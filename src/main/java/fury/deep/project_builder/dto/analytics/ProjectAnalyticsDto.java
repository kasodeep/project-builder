package fury.deep.project_builder.dto.analytics;

import java.util.Map;

public record ProjectAnalyticsDto(
        int totalTasks,
        int completedTasks,
        int blockedTasks,
        double completionRatio,
        Map<String, Integer> statusDistribution
) {
}
