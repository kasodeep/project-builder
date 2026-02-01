package fury.deep.project_builder.dto.project;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Request to create a new project with basic data such as name, start-date and end-date.
 * We can also use custom annotation to validate date criteria.
 *
 * @author night_fury_44
 */
public record ProjectCreateRequest(
        @NotBlank String name,
        @NotBlank LocalDate start,
        @NotBlank LocalDate end
) {
}
