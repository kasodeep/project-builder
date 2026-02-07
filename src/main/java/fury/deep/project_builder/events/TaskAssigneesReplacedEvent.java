package fury.deep.project_builder.events;

public record TaskAssigneesReplacedEvent(
        String taskId,
        String projectId
) implements AnalyticsEvent {
}

