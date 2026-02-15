package fury.deep.project_builder.events;

/**
 * Represents the events that occur on changes to the tasks of a project.
 * It allows to calculate the metrics for dashboard async with scalability and independence.
 *
 * @author night_fury_44
 */
public sealed interface AnalyticsEvent
        permits TaskCreatedEvent,
        TaskStatusChangedEvent,
        TaskDependenciesReplacedEvent,
        TaskAssigneesReplacedEvent,
        TaskUpdatedEvent, TaskDeletedEvent {

    String projectId();
}

