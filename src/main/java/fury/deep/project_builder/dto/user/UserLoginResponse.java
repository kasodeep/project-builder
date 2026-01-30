package fury.deep.project_builder.dto.user;

// For future extension we can send the token as well, for further authentication of each request.
public record UserLoginResponse(
        String message
) {
}
