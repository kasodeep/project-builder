package fury.deep.project_builder.security;

import fury.deep.project_builder.entity.user.User;

/**
 * Provides security context to each service, as user is added during basic authentication.
 * It can be configured to store any type of data.
 *
 * @author night_fury_44
 */
public class AuthContextHolder {

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static void setUser(User user) {
        currentUser.set(user);
    }

    public static User getUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}

