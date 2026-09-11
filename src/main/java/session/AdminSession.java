package session;

public final class AdminSession {

    private static Long adminUserId;
    private static String username;
    private static String fullName;
    private static String token;

    private AdminSession() {
    }

    public static void start(
            Long userId,
            String usernameValue,
            String fullNameValue,
            String tokenValue
    ) {

        adminUserId = userId;
        username = usernameValue;
        fullName = fullNameValue;
        token = tokenValue;
    }

    public static boolean isLoggedIn() {
        return token != null && !token.isBlank();
    }

    public static Long getAdminUserId() {
        return adminUserId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getFullName() {
        return fullName;
    }

    public static String getToken() {
        return token;
    }

    public static void clear() {

        adminUserId = null;
        username = null;
        fullName = null;
        token = null;
    }
}