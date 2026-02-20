package fury.deep.project_builder.dto.analytics.db;

import lombok.Data;

/**
 * Use for mapping analytics.
 */
@Data
public class DependencyAggRow {
    private String projectId;
    private int total;
    private int blocked;
    private int criticalPath;
    private double density;
}