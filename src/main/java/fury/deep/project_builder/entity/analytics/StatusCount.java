package fury.deep.project_builder.entity.analytics;

/**
 * Status overview regarding how many tasks being completed, locked or pending.
 *
 * @author night_fury_44
 */
public record StatusCount(
        String status,
        int count
) {
}
