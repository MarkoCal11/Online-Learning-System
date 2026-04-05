package hr.javafx.onlinelearningsystem.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashing {

    private static final Integer SALT_ROUNDS = 12;

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(SALT_ROUNDS));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    private PasswordHashing() {}
}