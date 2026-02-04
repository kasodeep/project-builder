package fury.deep.project_builder.dto.task.util;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AddDependenciesRequest(
        @NotBlank String taskId,
        @NotBlank
        List<@NotBlank(message = "dependency id cannot be blank") String> dependencies
) {
}
