package fury.deep.project_builder.dto.analytics;

import java.math.BigDecimal;

public record DependencyRiskDto(
        int totalDependencies,
        int blockedDependencyCount,
        int criticalPathLength,
        BigDecimal dependencyDensity,
        int riskScore
) {}
