package assignment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValidationAndHasherTest {

    @Test
    void delimiterAndLineBreakCharactersAreRejected() {
        assertTrue(Validation.hasDelimiterChars("ok", "a|b"));
        assertTrue(Validation.hasDelimiterChars("line\nbreak"));
        assertTrue(Validation.hasDelimiterChars("carriage\rreturn"));
        assertFalse(Validation.hasDelimiterChars("Jalan Mawar, 50000", "alex@gmail.com", null));
    }

    @Test
    void injectedFieldCannotChangeARecord() {
        // without the check this username would add extra columns to the user's line
        assertTrue(Validation.hasDelimiterChars("evil|hash|Admin"));
    }

    @Test
    void hashesAreSaltedSoTheSamePasswordHashesDifferently() {
        String a = PasswordHasher.hash("password1");
        String b = PasswordHasher.hash("password1");
        assertNotEquals(a, b);
        assertTrue(PasswordHasher.verify("password1", a));
        assertTrue(PasswordHasher.verify("password1", b));
    }

    @Test
    void malformedStoredValuesNeverVerify() {
        assertFalse(PasswordHasher.verify("x", "pbkdf2$onlyonepart"));
        assertFalse(PasswordHasher.verify(null, "pbkdf2$aa$bb"));
        assertFalse(PasswordHasher.verify("x", null));
    }

    @Test
    void adminPasswordDefaultsToDemoValueUnlessEnvOverrides() {
        if (System.getenv("HMS_ADMIN_PASSWORD") == null) {
            assertTrue(AdminCredentials.matches("123456"));
        }
        assertFalse(AdminCredentials.matches("definitely-not-the-password"));
    }
}
