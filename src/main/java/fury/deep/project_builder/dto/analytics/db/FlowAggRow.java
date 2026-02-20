package fury.deep.project_builder.dto.analytics.db;

import lombok.Data;

/**
 * Use for mapping analytics.
 */
@Data
public class FlowAggRow {
    private String projectId;
    private int wip;
    private int t7;
    private int t30;
    private int avgCycle;
}