package fury.deep.project_builder.repository.analytics;

import fury.deep.project_builder.dto.analytics.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnalyticsReadMapper {

    ProjectHealthDto findProjectHealth(
            @Param("projectId") String projectId
    );

    ProjectFlowDto findProjectFlow(
            @Param("projectId") String projectId
    );

    DependencyRiskDto findDependencyRisk(
            @Param("projectId") String projectId
    );

    TeamCapacityDto findTeamCapacity(
            @Param("projectId") String projectId
    );
}
