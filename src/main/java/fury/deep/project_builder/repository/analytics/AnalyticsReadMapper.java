package fury.deep.project_builder.repository.analytics;

import fury.deep.project_builder.entity.analytics.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnalyticsReadMapper {

    ProjectAnalytics findProjectAnalytics(
            @Param("projectId") String projectId
    );

    List<StatusCount> findStatusDistribution(
            @Param("projectId") String projectId
    );

    List<FeatureAnalytics> findFeatureAnalytics(
            @Param("projectId") String projectId
    );

    List<UserRisk> findUserDependencyRisk(
            @Param("projectId") String projectId
    );
}

