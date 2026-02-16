package fury.deep.project_builder.controller;

import fury.deep.project_builder.entity.task.Feature;
import fury.deep.project_builder.service.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Feature", description = "Feature management APIs")
@RestController
@RequestMapping("/api/v1/feature")
public class FeatureController {

    private final FeatureService featureService;

    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @Operation(
            summary = "Get all features",
            description = "Fetches all available features"
    )
    @GetMapping("/all")
    public ResponseEntity<List<Feature>> findAllFeatures() {
        return ResponseEntity.ok(featureService.findAllFeatures());
    }
}
