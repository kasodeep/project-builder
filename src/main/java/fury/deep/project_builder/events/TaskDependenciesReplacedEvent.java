package fury.deep.project_builder.events;

public record TaskDependenciesReplacedEvent(
        String taskId,
        String projectId
) implements AnalyticsEvent {
}

