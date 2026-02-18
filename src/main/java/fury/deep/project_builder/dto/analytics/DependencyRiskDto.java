package fury.deep.project_builder.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DependencyRiskDto {
    private int totalDependencies;
    private int blockedDependencyCount;
    private int criticalPathLength;
    private BigDecimal dependencyDensity;
    private int riskScore;
}
