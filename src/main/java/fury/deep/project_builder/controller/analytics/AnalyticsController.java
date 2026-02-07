package fury.deep.project_builder.controller.analytics;

import fury.deep.project_builder.dto.analytics.DashboardAnalyticsDto;
import fury.deep.project_builder.service.analytics.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<DashboardAnalyticsDto> projectDashboard(@PathVariable String projectId) {
        DashboardAnalyticsDto dashboard = analyticsService.getDashboard(projectId);
        return ResponseEntity.ok(dashboard);
    }
}
