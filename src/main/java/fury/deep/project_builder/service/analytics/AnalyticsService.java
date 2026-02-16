package fury.deep.project_builder.service.analytics;

import fury.deep.project_builder.dto.analytics.*;
import fury.deep.project_builder.repository.analytics.AnalyticsReadMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service class to fetch all the analytics for a given project.
 *
 */
@Service
public class AnalyticsService {

    private final AnalyticsReadMapper mapper;

    public AnalyticsService(AnalyticsReadMapper mapper) {
        this.mapper = mapper;
    }

    @Cacheable("project-dashboard")
    public DashboardAnalyticsDto getDashboard(String projectId) {
        return mapper.findDashboard(projectId);
    }
}
