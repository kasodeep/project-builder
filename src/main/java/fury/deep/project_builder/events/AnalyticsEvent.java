package fury.deep.project_builder.events;

public sealed interface AnalyticsEvent
        permits TaskCreatedEvent,
        TaskStatusChangedEvent,
        TaskDependenciesReplacedEvent,
        TaskAssigneesReplacedEvent {

    String projectId();
}

