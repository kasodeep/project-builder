package fury.deep.project_builder.service;

import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.entity.task.Feature;
import fury.deep.project_builder.exception.ResourceNotFoundException;
import fury.deep.project_builder.repository.FeatureMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureService {

    private final FeatureMapper featureMapper;

    public FeatureService(FeatureMapper featureMapper) {
        this.featureMapper = featureMapper;
    }

    public List<Feature> findAllFeatures() {
        return featureMapper.findAll();
    }

    public Feature findById(String id) {
        Feature feature = featureMapper.findById(id);

        if (feature == null) throw new ResourceNotFoundException(ErrorMessages.FEATURE_NOT_FOUND.formatted(id));
        return feature;
    }

    public void existsById(String id) {
        if (!featureMapper.existsById(id)) {
            throw new ResourceNotFoundException(ErrorMessages
                    .FEATURE_NOT_FOUND
                    .formatted(id));
        }
    }
}
