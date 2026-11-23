package assignment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// The built-in "admin" account. Its password is read from the HMS_ADMIN_PASSWORD environment variable.
public final class AdminCredentials {
    // Demo default so the project runs out of the box. Set HMS_ADMIN_PASSWORD to override it.
    private static final String DEFAULT_PASSWORD = "123456";

    private AdminCredentials() {}

    public static boolean matches(String password) {
        String expected = System.getenv().getOrDefault("HMS_ADMIN_PASSWORD", DEFAULT_PASSWORD);
        return MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }
}
