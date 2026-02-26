package fury.deep.project_builder.dto.analytics.db;

import lombok.Data;

/**
 * Use for mapping analytics.
 */
@Data
public class TeamAggRow {
    private String teamId;
    private int    activeProjects;
    private int    activeTasks;
    private double avgTasks;
    private int    activeUsers;
    private int    overloaded;
    private double avgCompletion;
}