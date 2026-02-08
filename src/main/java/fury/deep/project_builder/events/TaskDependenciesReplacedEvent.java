package fury.deep.project_builder.events;

/**
 * The event is fired when the dependencies of a task are replaced.
 *
 * @author night_fury_44
 */
public record TaskDependenciesReplacedEvent(
        String taskId,
        String projectId
) implements AnalyticsEvent {
}

