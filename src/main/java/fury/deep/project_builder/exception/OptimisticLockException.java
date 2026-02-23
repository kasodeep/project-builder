package fury.deep.project_builder.exception;

/**
 * Thrown when an update is attempted on a stale version of an entity.
 * Maps to HTTP 409 Conflict via GlobalExceptionHandler.
 */
public class OptimisticLockException extends RuntimeException {
    public OptimisticLockException(String message) {
        super(message);
    }
}
