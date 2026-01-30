package fury.deep.project_builder.exception;

public record ErrorResponse(
        String message
) {
    public static ErrorResponse of(String message) {
        return new ErrorResponse(message);
    }
}