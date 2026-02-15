package fury.deep.project_builder.dto.task;

import fury.deep.project_builder.entity.task.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @NotBlank String id,
        @NotBlank String name,
        @NotBlank String featureId,
        @Min(value = 1, message = "Minimum priority will be 1")
        @Max(value = 10, message = "Maximum priority will be 10")
        Integer priority,
        Status status,
        @NotNull LocalDate start,
        @NotNull LocalDate end
) {
}
