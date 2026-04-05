package hr.javafx.onlinelearningsystem.util;

import hr.javafx.onlinelearningsystem.model.UserProfile;

import java.util.Optional;

public class Session {

    private static Session instance;
    private UserProfile currentUser;

    private Session() {}

    public static synchronized Session getInstance() {
        if(Optional.ofNullable(instance).isEmpty()) {
            instance = new Session();
        }
        return instance;
    }

    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
    }

    public UserProfile getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        this.currentUser = null;
    }
}
