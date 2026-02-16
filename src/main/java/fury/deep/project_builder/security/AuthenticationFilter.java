package fury.deep.project_builder.security;

import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.repository.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * A filter acting as a security guard for NON_WHITELIST_URLS.
 * It checks for basic authentication and updated the context with the authenticated user.
 *
 * @author night_fury_44
 */
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final String[] WHITELIST_URLS = {
            "/api/v1/auth",
            "/api/v1/team",
            "/api/v1/feature",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    };

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationFilter(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * The method filters the WHITELIST_URLS and authenticate the secured ones.
     *
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain
    ) throws IOException, ServletException {

        try {
            if (shouldNotFilter(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }

            if (!authenticate(authHeader)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
            filterChain.doFilter(request, response);
        } finally {
            AuthContextHolder.clear();
        }
    }

    /**
     * It decodes the header containing credentials, matches the password for the user, and sets the context.
     *
     * @return - true when the auth is a success.
     */
    private boolean authenticate(String authHeader) {
        String base64Credentials = authHeader.substring(6);

        byte[] decoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(decoded, StandardCharsets.UTF_8);

        String[] parts = credentials.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String username = parts[0];
        String password = parts[1];

        User user = userMapper.findByUsername(username);
        if (user == null) {
            return false;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return false;
        }

        AuthContextHolder.setUser(user);
        return true;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Allow preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        for (String check : WHITELIST_URLS) {
            if (path.startsWith(check)) return true;
        }
        return false;
    }
}

