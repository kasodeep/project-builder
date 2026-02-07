package fury.deep.project_builder.events;

import fury.deep.project_builder.entity.task.Status;

import java.time.Instant;

public record TaskStatusChangedEvent(
        String taskId,
        String projectId,
        Status oldStatus,
        Status newStatus,
        Instant occurredAt
) implements AnalyticsEvent {
}

