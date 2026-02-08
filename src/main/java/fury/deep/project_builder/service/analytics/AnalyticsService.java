package fury.deep.project_builder.service.analytics;

import fury.deep.project_builder.dto.analytics.*;
import fury.deep.project_builder.entity.analytics.*;
import fury.deep.project_builder.repository.analytics.AnalyticsReadMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final AnalyticsReadMapper mapper;

    public AnalyticsService(AnalyticsReadMapper mapper) {
        this.mapper = mapper;
    }

    @Cacheable("project-dashboard")
    public DashboardAnalyticsDto getDashboard(String projectId) {
        ProjectAnalytics project = mapper.findProjectAnalytics(projectId);

        Map<String, Integer> statusDistribution =
                mapper.findStatusDistribution(projectId)
                        .stream()
                        .collect(Collectors.toMap(
                                StatusCount::status,
                                StatusCount::count
                        ));

        double completionRatio =
                project.totalTasks() == 0 ? 0
                        : (double) project.completedTasks() / project.totalTasks();

        ProjectAnalyticsDto projectDto =
                new ProjectAnalyticsDto(
                        project.totalTasks(),
                        project.completedTasks(),
                        project.blockedTasks(),
                        completionRatio,
                        statusDistribution
                );

        /* ---------- FEATURES ---------- */
        List<FeatureAnalyticsDto> features =
                mapper.findFeatureAnalytics(projectId)
                        .stream()
                        .map(f -> new FeatureAnalyticsDto(
                                f.featureId(),
                                f.featureName(),
                                f.totalTasks(),
                                f.completedTasks(),
                                f.blockedTasks(),
                                f.totalTasks() == 0
                                        ? 0
                                        : (double) f.completedTasks() / f.totalTasks(),
                                f.blockedTasks() > 0
                        ))
                        .toList();

        /* ---------- USERS ---------- */
        List<UserDependencyRiskDto> users =
                mapper.findUserDependencyRisk(projectId)
                        .stream()
                        .map(u -> new UserDependencyRiskDto(
                                u.userId(),
                                u.username(),
                                u.blockingTasks(),
                                u.blockedUsers(),
                                u.riskScore()
                        ))
                        .toList();

        return new DashboardAnalyticsDto(projectDto, features, users);
    }
}
