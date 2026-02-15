package fury.deep.project_builder.events;

import fury.deep.project_builder.entity.task.Status;

/**
 * The event is fired when the status of a task has been modified.
 *
 * @author night_fury_44
 */
public record TaskStatusChangedEvent(
        String taskId,
        String projectId,
        Status oldStatus,
        Status newStatus
) implements AnalyticsEvent {
}

