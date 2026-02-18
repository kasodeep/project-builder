package fury.deep.project_builder.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFlowDto {

    private int wipCount;
    private int throughput7d;
    private int throughput30d;
    private int avgCycleTime;
    private int reopenedTasks;
}
