package fury.deep.project_builder.exception;

import java.time.Instant;

/**
 * Defines the contract of how the error messages should be sent, when an error occurs.
 *
 * @author night_fury_44
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String path
) {
    public static ErrorResponse of(int status, String error, String path) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                path
        );
    }
}
