package assignment;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;

// Reads and writes the user files (managers.txt, staffs.txt, doctors.txt, patients.txt).
// One user per line, fields separated by "|". Field 0 is the ID, 1 the username, 2 the password.
// Passwords are always stored hashed: any plain-text password passed to a write method is hashed here,
// so every screen that edits users is covered without changing its own code.
public class UserFileHandler {

    // Directory holding the data files; the app's working directory (data/) by default. Tests point it elsewhere.
    static Path baseDir = Paths.get("");

    private static final SecureRandom RANDOM = new SecureRandom();

    private static Path fileFor(String role) {
        return baseDir.resolve(role.toLowerCase() + "s.txt");
    }

    // ---------------- READ ----------------
    // Reads all users of a given role. Each line is split by "|" (trailing empty fields are kept).
    public static List<String[]> readUsersFromFile(String role) {
        List<String[]> users = new ArrayList<>();
        Path file = fileFor(role);
        if (!Files.exists(file)) return users;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) users.add(line.split("\\|", -1));
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + file + " (" + e.getMessage() + ")");
        }
        return users;
    }

    public static String[] findUserById(String role, String id) {
        for (String[] user : readUsersFromFile(role)) {
            if (user.length > 0 && user[0].equals(id)) return user;
        }
        return null;
    }

    // ---------------- WRITE ----------------
    // Writes all users back to the role's file. Plain-text passwords are hashed first.
    // The file is replaced atomically, so a failure part-way never leaves a truncated file.
    public static boolean writeUsersToFile(String role, List<String[]> users) {
        Path file = fileFor(role);
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        List<String> lines = new ArrayList<>();
        for (String[] user : users) {
            String[] row = user.clone();
            if (row.length > 2 && !row[2].isEmpty() && !PasswordHasher.isHashed(row[2])) {
                row[2] = PasswordHasher.hash(row[2]);
            }
            lines.add(String.join("|", row));
        }
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing file: " + file + " (" + e.getMessage() + ")");
            return false;
        }
    }

    // Adds one user. Returns false if the file could not be written.
    public static boolean appendUser(String role, String[] row) {
        List<String[]> users = readUsersFromFile(role);
        users.add(row);
        return writeUsersToFile(role, users);
    }

    // ---------------- DELETE ----------------
    public static void deleteUserById(String role, String id) {
        List<String[]> users = readUsersFromFile(role);
        users.removeIf(user -> user[0].equals(id));
        writeUsersToFile(role, users);
    }

    // ---------------- UPDATE ----------------
    // Replaces the user with the given ID. An empty password in newData keeps the current password.
    public static void updateUserById(String role, String id, String[] newData) {
        List<String[]> users = readUsersFromFile(role);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i)[0].equals(id)) {
                String[] row = newData.clone();
                if (row.length > 2 && row[2].isEmpty() && users.get(i).length > 2) row[2] = users.get(i)[2];
                users.set(i, row);
                break;
            }
        }
        writeUsersToFile(role, users);
    }

    // ---------------- IDs ----------------
    // Next free ID for a role: role initial + 5 digits (e.g. P48213), never one that is already in use.
    public static String nextId(String role) {
        Set<String> used = new HashSet<>();
        for (String[] user : readUsersFromFile(role)) used.add(user[0]);
        String prefix = role.substring(0, 1).toUpperCase();
        String id;
        do {
            id = prefix + (10000 + RANDOM.nextInt(90000));
        } while (used.contains(id));
        return id;
    }

    // ---------------- MIGRATION ----------------
    // Hashes any plain-text passwords left in the data files (older versions stored them as text).
    public static void migratePasswords() {
        for (String role : new String[]{"manager", "staff", "doctor", "patient"}) {
            List<String[]> users = readUsersFromFile(role);
            boolean needsMigration = users.stream().anyMatch(u -> u.length > 2 && !u[2].isEmpty() && !PasswordHasher.isHashed(u[2]));
            if (needsMigration) writeUsersToFile(role, users);
        }
    }
}
