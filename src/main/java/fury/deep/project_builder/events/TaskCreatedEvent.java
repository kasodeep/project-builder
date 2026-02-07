package fury.deep.project_builder.events;

import fury.deep.project_builder.entity.task.Status;

public record TaskCreatedEvent(
        String taskId,
        String projectId,
        Status status
) implements AnalyticsEvent {
}


