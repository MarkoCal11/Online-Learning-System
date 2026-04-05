package hr.javafx.onlinelearningsystem.auth;

import hr.javafx.onlinelearningsystem.enums.UserRole;

public record AuthResult(String username, UserRole role) {}
