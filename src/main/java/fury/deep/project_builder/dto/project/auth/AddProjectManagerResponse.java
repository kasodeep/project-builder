package fury.deep.project_builder.dto.project.auth;

import java.util.List;

public record AddProjectManagerResponse(
        String projectId,
        List<String> managers
) {
}
