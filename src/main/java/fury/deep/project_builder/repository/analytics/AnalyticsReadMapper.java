package fury.deep.project_builder.repository.analytics;

import fury.deep.project_builder.dto.analytics.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnalyticsReadMapper {

    DashboardAnalyticsDto findDashboard(
            @Param("projectId") String projectId
    );
}

