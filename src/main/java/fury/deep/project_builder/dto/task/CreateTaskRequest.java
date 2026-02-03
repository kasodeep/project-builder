package fury.deep.project_builder.dto.task;

import fury.deep.project_builder.entity.task.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request for creating a task, belonging to a project, categorized by feature.
 * The priority scale is assumed to be between 1-10.
 *
 * @author night_fury_44
 */
public record CreateTaskRequest(
        @NotBlank String name,
        @NotBlank String projectId,
        @NotBlank String featureId,
        @Min(value = 1, message = "Minimum priority will be 1")
        @Max(value = 10, message = "Maximum priority will be 10")
        Integer priority,
        Status status,
        @NotNull LocalDate start,
        @NotNull LocalDate end
) {
}
