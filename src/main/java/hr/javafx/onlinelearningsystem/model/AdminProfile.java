package hr.javafx.onlinelearningsystem.model;

import hr.javafx.onlinelearningsystem.enums.UserRole;

public final class AdminProfile extends AbstractProfile{

    public AdminProfile(Long id, String username) {
        super(id, username, UserRole.ADMIN);
    }

}
