package fury.deep.project_builder.dto.project.auth;

import java.util.List;

/**
 * Response for the project update request with all the managers.
 *
 * @author night_fury_44
 */
public record AddProjectManagerResponse(
        String projectId,
        List<String> managers
) {
}
