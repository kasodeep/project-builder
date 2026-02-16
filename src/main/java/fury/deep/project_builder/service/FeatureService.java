package fury.deep.project_builder.service;

import fury.deep.project_builder.constants.ErrorMessages;
import fury.deep.project_builder.entity.task.Feature;
import fury.deep.project_builder.exception.ResourceNotFoundException;
import fury.deep.project_builder.repository.FeatureMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * List of service method providing exposure to feature.
 *
 * @author night_fury_44
 */
@Service
public class FeatureService {

    private final FeatureMapper featureMapper;

    public FeatureService(FeatureMapper featureMapper) {
        this.featureMapper = featureMapper;
    }

    /**
     * Method to find all the features present in the db.
     *
     */
    public List<Feature> findAllFeatures() {
        return featureMapper.findAll();
    }

    /**
     * Method to find a feature by its id.
     *
     */
    public Feature findById(String id) {
        Feature feature = featureMapper.findById(id);

        if (feature == null) throw new ResourceNotFoundException(ErrorMessages.FEATURE_NOT_FOUND.formatted(id));
        return feature;
    }
}
