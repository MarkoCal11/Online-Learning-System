package hr.javafx.onlinelearningsystem.model;

import hr.javafx.onlinelearningsystem.enums.UserRole;

public abstract sealed class AbstractProfile extends Entity implements Profile permits AdminProfile, UserProfile{

    private String username;
    private UserRole role;

    protected AbstractProfile(String username, UserRole role) {
        super();
        this.username = username;
        this.role=role;
    }

    protected AbstractProfile(Long id, String username, UserRole role) {
        super(id);
        this.username = username;
        this.role=role;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {this.username = username;}

    @Override
    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }
}