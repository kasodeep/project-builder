package fury.deep.project_builder.service.analytics;

import fury.deep.project_builder.dto.analytics.*;
import fury.deep.project_builder.repository.analytics.AnalyticsReadMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final AnalyticsReadMapper mapper;

    public AnalyticsService(AnalyticsReadMapper mapper) {
        this.mapper = mapper;
    }

    @Cacheable("project-dashboard")
    public DashboardAnalyticsDto getDashboard(String projectId) {

        ProjectHealthDto health = mapper.findProjectHealth(projectId);
        ProjectFlowDto flow = mapper.findProjectFlow(projectId);
        DependencyRiskDto dependency = mapper.findDependencyRisk(projectId);
        TeamCapacityDto team = mapper.findTeamCapacity(projectId);

        return new DashboardAnalyticsDto(
                health,
                flow,
                dependency,
                team
        );
    }
}
