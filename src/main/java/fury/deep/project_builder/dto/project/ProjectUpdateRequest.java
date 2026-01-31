package fury.deep.project_builder.dto.project;

import java.time.LocalDate;

public record ProjectUpdateRequest(
        String projectId,
        String name,
        LocalDate start,
        LocalDate end
) {
}

