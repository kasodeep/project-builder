package fury.deep.project_builder.events;

import fury.deep.project_builder.entity.task.Status;

/**
 * The event is fired when a new task is created for the project.
 *
 * @author night_fury_44
 */
public record TaskCreatedEvent(
        String taskId,
        String projectId,
        Status status
) implements AnalyticsEvent {
}


