package fury.deep.project_builder.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request payload for PATCH /api/v1/project/update.
 */
public record ProjectUpdateRequest(
        @NotBlank String projectId,
        @NotNull Long version,
        String name,
        LocalDate start,
        LocalDate end
) {
}