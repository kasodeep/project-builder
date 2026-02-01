package fury.deep.project_builder.dto.project;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Request to update the project details, in-case of changed in start-date, end-date.
 *
 * @author night_fury_44
 */
public record ProjectUpdateRequest(
        @NotBlank String projectId,
        @NotBlank String name,
        @NotBlank LocalDate start,
        @NotBlank LocalDate end
) {
}

