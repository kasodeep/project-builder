package fury.deep.project_builder.dto.analytics.db;

import lombok.Data;

/**
 * Use for mapping analytics.
 */
@Data
public class HealthAggRow {
    private String projectId;
    private int overdue;
    private int blocked;
    private int longRunning;
}