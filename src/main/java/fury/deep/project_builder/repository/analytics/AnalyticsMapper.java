package fury.deep.project_builder.repository.analytics;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnalyticsMapper {

    /* ---------- ENSURE ---------- */
    void ensureProjectHealth(String projectId);
    void ensureFlow(String projectId);
    void ensureDependency(String projectId);
    void ensureTeamCapacity(String teamId);

    /* ---------- FLOW ---------- */
    int countActiveWip(String projectId);
    int countThroughput7d(String projectId);
    int countThroughput30d(String projectId);
    double calculateAvgCycleTime(String projectId);

    void updateFlow(@Param("projectId") String projectId,
                    @Param("wip") int wip,
                    @Param("throughput7d") int throughput7d,
                    @Param("throughput30d") int throughput30d,
                    @Param("avgCycle") int avgCycleDays);

    int countLongRunningTasks(String projectId);


    /* ---------- DEPENDENCY ---------- */
    int countDependencies(String projectId);
    int countBlockedDependencies(String projectId);
    int countTasks(String projectId);
    int calculateCriticalPath(String projectId);

    void updateDependencyRisk(@Param("projectId") String projectId,
                              @Param("total") int total,
                              @Param("blocked") int blocked,
                              @Param("criticalPath") int criticalPath,
                              @Param("density") double density,
                              @Param("risk") int risk);

    /* ---------- TEAM ---------- */
    int countActiveTasksByTeam(String teamId);
    double calculateAvgTasksPerUser(String teamId);
    int countOverloadedUsers(@Param("teamId") String teamId,
                             @Param("threshold") int threshold);
    int countActiveProjectsByTeam(String teamId);
    double calculateAvgCompletionTime(String teamId);

    void updateTeamCapacity(@Param("teamId") String teamId,
                            @Param("activeProjects") int activeProjects,
                            @Param("activeTasks") int activeTasks,
                            @Param("avgTasks") int avgTasks,
                            @Param("overloaded") int overloaded,
                            @Param("avgCompletion") int avgCompletion,
                            @Param("burnout") int burnout);

    /* ---------- HEALTH ---------- */
    int countOverdue(String projectId);

    void updateProjectHealth(@Param("projectId") String projectId,
                             @Param("variance") int variance,
                             @Param("overdue") int overdue,
                             @Param("blocked") int blocked,
                             @Param("score") int score,
                             @Param("riskLevel") String riskLevel);

    /* ---------- LOOKUPS ---------- */
    String findTeamByProject(String projectId);
}
