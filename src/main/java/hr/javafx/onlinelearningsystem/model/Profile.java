package hr.javafx.onlinelearningsystem.model;

import hr.javafx.onlinelearningsystem.enums.UserRole;

public sealed interface Profile permits AbstractProfile {

    String getUsername();
    UserRole getRole();
}