package fury.deep.project_builder.events;

/**
 * The event is fired when the assignees of a task are replaced.
 *
 * @author night_fury_44
 */
public record TaskAssigneesReplacedEvent(
        String taskId,
        String projectId
) implements AnalyticsEvent {
}

