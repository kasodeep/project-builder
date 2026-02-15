package fury.deep.project_builder.events;

public record TaskDeletedEvent(
        String taskId,
        String projectId
) implements AnalyticsEvent {
}

