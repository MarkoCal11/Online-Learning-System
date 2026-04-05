package hr.javafx.onlinelearningsystem.auth;

import hr.javafx.onlinelearningsystem.enums.UserRole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static hr.javafx.onlinelearningsystem.auth.PasswordHashing.hashPassword;

public class UserCredentialsService {

    private static final Path USER_FILE = Path.of("dat/users.txt");

    public Map<String, UserCredential> loadCredentials() throws IOException {
        Map<String, UserCredential> credentials = new HashMap<>();

        try(Stream<String> lines = Files.lines(USER_FILE)) {
            lines.forEach(line -> {
                String[] parts = line.split(";");

                if (parts.length != 3) return;

                String username = parts[0];
                String hashedPassword = parts[1];
                UserRole role = UserRole.valueOf(parts[2]);

                credentials.put(username, new UserCredential(username, hashedPassword, role));
            });
        }
        return credentials;
    }

    public void saveAll(Map<String, UserCredential> users) throws IOException {
        List<String> lines = users.values().stream()
                .map(u -> u.username() + ";" +
                        u.hashedPassword() + ";" +
                        u.role())
                .toList();

        Files.write(USER_FILE, lines);
    }

    public void addUserToFile(String username, String password, UserRole role) throws IOException {
        Map<String, UserCredential> users = loadCredentials();
        String hashedPassword = hashPassword(password);
        users.put(username, new UserCredential(username, hashedPassword, role));
        saveAll(users);
    }

    public void removeUserFromFile(String username) throws IOException {
        Map<String, UserCredential> users = loadCredentials();
        users.remove(username);
        saveAll(users);
    }

    public void updateUsernameInFile(String oldUsername, String newUsername) throws IOException {
        Map<String, UserCredential> users = loadCredentials();
        UserCredential currentCredentials = users.remove(oldUsername);
        users.put(newUsername, new UserCredential(newUsername, currentCredentials.hashedPassword(), currentCredentials.role()));
        saveAll(users);
    }

    public void updatePasswordInFile(String username, String newPassword) throws IOException {
        Map<String, UserCredential> users = loadCredentials();
        UserCredential currentCredentials = users.get(username);
        String hashedPassword = hashPassword(newPassword);
        users.put(username, new UserCredential(username, hashedPassword, currentCredentials.role()));
        saveAll(users);
    }
}