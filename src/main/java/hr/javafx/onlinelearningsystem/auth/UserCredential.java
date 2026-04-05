package hr.javafx.onlinelearningsystem.auth;

import hr.javafx.onlinelearningsystem.enums.UserRole;

public record UserCredential(String username, String hashedPassword, UserRole role) {}