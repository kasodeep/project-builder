package fury.deep.project_builder.repository.analytics;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnalyticsMapper {

    /* ---------- ENSURE ---------- */
    void ensureProjectHealth(String projectId);
    void ensureFlow(String projectId);
    void ensureDependency(String projectId);
    void ensureTeamCapacity(String teamId);

    /* ---------- FLOW ---------- */
    void updateWip(String projectId, int wip);
    void incrementThroughput(String projectId);

    /* ---------- DEPENDENCY ---------- */
    void updateDependencyRisk(String projectId,
                              int total,
                              int blocked,
                              double density,
                              int risk);

    /* ---------- TEAM ---------- */
    void updateTeamCapacity(String teamId,
                            int activeTasks,
                            int avgTasks,
                            int overloaded,
                            int burnout);

    /* ---------- HEALTH ---------- */
    void updateProjectHealth(String projectId,
                             int variance,
                             int overdue,
                             int blocked,
                             int score,
                             String riskLevel);

    /* ---------- LOOKUPS ---------- */
    int countWip(String projectId);
    int countOverdue(String projectId);
    int countDependencies(String projectId);
    int countBlockedDependencies(String projectId);
    String findTeamByProject(String projectId);
    int countActiveTasksByTeam(String teamId);
}

