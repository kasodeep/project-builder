package fury.deep.project_builder.constants;

/**
 * A common class for all the error messages.
 * The parameter messages must be used with formatted() method call.
 *
 * @author night_fury_44
 *
 */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static final String TEAM_NOT_FOUND =
            "Team not found with id: %s";

    public static final String FEATURE_NOT_FOUND =
            "Feature not found with id: %s";

    public static final String TASK_NOT_FOUND =
            "Task not found with id: %s";

    public static final String USER_NOT_FOUND =
            "User not found with id: %s";

    public static final String PROJECT_NOT_FOUND =
            "Project not found with id: %s";

    public static final String UNAUTHORIZED_ACTION =
            "You are not authorized to perform this action";

    public static final String INVALID_CREDENTIALS =
            "Invalid username or password";
}

