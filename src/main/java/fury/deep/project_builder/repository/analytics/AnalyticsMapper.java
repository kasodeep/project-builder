package fury.deep.project_builder.repository.analytics;

import fury.deep.project_builder.dto.analytics.db.DependencyAggRow;
import fury.deep.project_builder.dto.analytics.db.FlowAggRow;
import fury.deep.project_builder.dto.analytics.db.HealthAggRow;
import fury.deep.project_builder.dto.analytics.db.TeamAggRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnalyticsMapper {

    /* ---------- ENSURE (idempotent row creation) ---------- */
    void ensureProjectHealth(@Param("projectId") String projectId);

    void ensureFlow(@Param("projectId") String projectId);

    void ensureDependency(@Param("projectId") String projectId);

    void ensureTeamCapacity(@Param("teamId") String teamId);

    /* ---------- AGGREGATE SELECTS ---------- */
    FlowAggRow selectFlowAgg(@Param("projectId") String projectId);

    DependencyAggRow selectDependencyAgg(@Param("projectId") String projectId);

    HealthAggRow selectHealthAgg(@Param("projectId") String projectId);

    TeamAggRow selectTeamAgg(@Param("teamId") String teamId, @Param("overloadThreshold") int overloadThreshold);

    /* ---------- UPDATES ---------- */
    void updateFlow(
            @Param("projectId") String projectId,
            @Param("wip") int wip,
            @Param("throughput7d") int throughput7d,
            @Param("throughput30d") int throughput30d,
            @Param("avgCycle") int avgCycleDays);

    void updateDependencyRisk(
            @Param("projectId") String projectId,
            @Param("total") int total,
            @Param("blocked") int blocked,
            @Param("criticalPath") int criticalPath,
            @Param("density") double density,
            @Param("risk") int risk);

    void updateProjectHealth(
            @Param("projectId") String projectId,
            @Param("overdue") int overdue,
            @Param("blocked") int blocked,
            @Param("score") int score,
            @Param("riskLevel") String riskLevel);

    void updateTeamCapacity(
            @Param("teamId") String teamId,
            @Param("activeProjects") int activeProjects,
            @Param("activeTasks") int activeTasks,
            @Param("avgTasks") int avgTasks,
            @Param("overloaded") int overloaded,
            @Param("avgCompletion") int avgCompletion,
            @Param("burnout") int burnout);

    /* ---------- LOOKUPS ---------- */
    String findTeamByProject(@Param("projectId") String projectId);
}