package fury.deep.project_builder.dto.task.util;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request to replace the dependencies of a task.
 *
 * @author night_fury_44
 */
public record AddDependenciesRequest(
        @NotBlank String taskId,
        @NotNull
        List<@NotBlank(message = "dependency id cannot be blank") String> dependencies
) {
}
