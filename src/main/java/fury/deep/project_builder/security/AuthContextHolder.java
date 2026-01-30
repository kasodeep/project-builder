package fury.deep.project_builder.security;

import fury.deep.project_builder.entity.user.User;

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

