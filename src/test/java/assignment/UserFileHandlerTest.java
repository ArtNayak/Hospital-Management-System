package assignment;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserFileHandlerTest {

    @TempDir
    Path dir;

    @BeforeEach
    void useTempDir() {
        UserFileHandler.baseDir = dir;
    }

    private static List<String[]> rows(String[]... records) {
        return new ArrayList<>(Arrays.asList(records));
    }

    private static String[] patient(String id, String username, String password) {
        return new String[]{id, username, password, "First", "Last", "Female", "03/03/2002", "22",
                "a@b.com", "0123456789", "Street 1", "50000", "KL"};
    }

    @Test
    void plainPasswordsAreHashedOnWriteAndStillVerify() throws IOException {
        UserFileHandler.writeUsersToFile("patient", rows(patient("P10001", "ann", "secret1")));
        String raw = Files.readString(dir.resolve("patients.txt"));
        assertFalse(raw.contains("secret1"), "plain text must not be written to disk");

        String[] stored = UserFileHandler.readUsersFromFile("patient").get(0);
        assertTrue(PasswordHasher.isHashed(stored[2]));
        assertTrue(PasswordHasher.verify("secret1", stored[2]));
        assertFalse(PasswordHasher.verify("wrong", stored[2]));
    }

    @Test
    void hashingTwiceDoesNotRehash() {
        UserFileHandler.writeUsersToFile("patient", rows(patient("P10001", "ann", "secret1")));
        String first = UserFileHandler.readUsersFromFile("patient").get(0)[2];
        UserFileHandler.writeUsersToFile("patient", UserFileHandler.readUsersFromFile("patient"));
        assertEquals(first, UserFileHandler.readUsersFromFile("patient").get(0)[2]);
    }

    @Test
    void updateWithBlankPasswordKeepsTheOldHash() {
        UserFileHandler.writeUsersToFile("patient", rows(patient("P10001", "ann", "secret1")));
        String oldHash = UserFileHandler.findUserById("patient", "P10001")[2];

        String[] edited = patient("P10001", "ann", "");
        edited[3] = "Changed";
        UserFileHandler.updateUserById("patient", "P10001", edited);

        String[] after = UserFileHandler.findUserById("patient", "P10001");
        assertEquals("Changed", after[3]);
        assertEquals(oldHash, after[2]);
        assertTrue(PasswordHasher.verify("secret1", after[2]));
    }

    @Test
    void updateWithNewPasswordReplacesIt() {
        UserFileHandler.writeUsersToFile("patient", rows(patient("P10001", "ann", "secret1")));
        UserFileHandler.updateUserById("patient", "P10001", patient("P10001", "ann", "newpass9"));
        String stored = UserFileHandler.findUserById("patient", "P10001")[2];
        assertTrue(PasswordHasher.verify("newpass9", stored));
        assertFalse(PasswordHasher.verify("secret1", stored));
    }

    @Test
    void deleteRemovesOnlyTheMatchingUser() {
        List<String[]> users = new ArrayList<>();
        users.add(patient("P10001", "ann", "secret1"));
        users.add(patient("P10002", "bob", "secret2"));
        UserFileHandler.writeUsersToFile("patient", users);
        UserFileHandler.deleteUserById("patient", "P10001");
        assertNull(UserFileHandler.findUserById("patient", "P10001"));
        assertNotNull(UserFileHandler.findUserById("patient", "P10002"));
    }

    @Test
    void appendKeepsExistingUsersEvenWithoutTrailingNewline() throws IOException {
        Files.writeString(dir.resolve("patients.txt"), String.join("|", patient("P10001", "ann", "x")));  // no final newline
        assertTrue(UserFileHandler.appendUser("patient", patient("P10002", "bob", "secret2")));
        assertEquals(2, UserFileHandler.readUsersFromFile("patient").size());
    }

    @Test
    void migrationHashesLegacyPlainTextPasswordsAndLoginStillWorks() throws IOException {
        Files.writeString(dir.resolve("doctors.txt"),
                String.join("|", patient("D10001", "alex", "123456")) + "\n");
        UserFileHandler.migratePasswords();
        String stored = UserFileHandler.findUserById("doctor", "D10001")[2];
        assertTrue(PasswordHasher.isHashed(stored));
        assertTrue(PasswordHasher.verify("123456", stored));
    }

    @Test
    void legacyPlainTextStillVerifiesBeforeMigration() {
        assertTrue(PasswordHasher.verify("123456", "123456"));
        assertFalse(PasswordHasher.verify("1234567", "123456"));
    }

    @Test
    void missingFileReadsAsEmpty() {
        assertTrue(UserFileHandler.readUsersFromFile("manager").isEmpty());
    }

    @Test
    void nextIdUsesRolePrefixAndNeverReusesAnId() {
        for (int i = 0; i < 200; i++) {
            String id = UserFileHandler.nextId("patient");
            assertTrue(id.matches("P\\d{5}"), id);
            assertNull(UserFileHandler.findUserById("patient", id));
            UserFileHandler.appendUser("patient", patient(id, "u" + i, "secret1"));
        }
        long distinct = UserFileHandler.readUsersFromFile("patient").stream().map(u -> u[0]).distinct().count();
        assertEquals(200, distinct);
    }
}
