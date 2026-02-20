package fury.deep.project_builder.dto.analytics.db;

import lombok.Data;

/**
 * Use for mapping analytics.
 */
@Data
public class TeamAggRow {
    private String teamId;
    private int activeProjects;
    private int activeTasks;
    private int avgTasks;
    private int overloaded;
    private int avgCompletion;
}