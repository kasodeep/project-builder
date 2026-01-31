package fury.deep.project_builder.dto.project;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProjectCreateRequest(
        @NotNull String name,
        @NotNull LocalDate start,
        @NotNull LocalDate end
) {
}
