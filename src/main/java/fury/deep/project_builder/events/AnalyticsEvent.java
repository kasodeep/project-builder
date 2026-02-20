package fury.deep.project_builder.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents the events that occur on changes to the tasks of a project.
 * It allows to calculate the metrics for dashboard async with scalability and independence.
 *
 * @author night_fury_44
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TaskCreatedEvent.class, name = "TaskCreatedEvent"),
        @JsonSubTypes.Type(value = TaskStatusChangedEvent.class, name = "TaskStatusChangedEvent"),
        @JsonSubTypes.Type(value = TaskDependenciesReplacedEvent.class, name = "TaskDependenciesReplacedEvent"),
        @JsonSubTypes.Type(value = TaskAssigneesReplacedEvent.class, name = "TaskAssigneesReplacedEvent"),
        @JsonSubTypes.Type(value = TaskUpdatedEvent.class, name = "TaskUpdatedEvent"),
        @JsonSubTypes.Type(value = TaskDeletedEvent.class, name = "TaskDeletedEvent")
})
public sealed interface AnalyticsEvent
        permits TaskCreatedEvent,
        TaskStatusChangedEvent,
        TaskDependenciesReplacedEvent,
        TaskAssigneesReplacedEvent,
        TaskUpdatedEvent, TaskDeletedEvent {

    String projectId();
}

