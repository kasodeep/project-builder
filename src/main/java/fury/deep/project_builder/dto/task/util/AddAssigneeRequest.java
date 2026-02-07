package fury.deep.project_builder.dto.task.util;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddAssigneeRequest(
        @NotBlank String taskId,
        @NotNull
        List<@NotBlank(message = "assignee id cannot be blank") String> assignees
) {
}
