package hr.javafx.onlinelearningsystem.model;

import hr.javafx.onlinelearningsystem.enums.UserRole;

import java.time.LocalDate;

public abstract sealed class UserProfile extends AbstractProfile permits TeacherProfile, StudentProfile{

    private String firstName;
    private String lastName;
    private String email;
    private final LocalDate dateAdded;

    private boolean requestedPasswordReset;
    private Integer coursesCount;


    protected UserProfile(String username, UserRole role, String firstName,
                          String lastName, String email, LocalDate dateAdded) {
        super(username, role);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateAdded = dateAdded;
    }

    protected UserProfile(Long id, String username, UserRole role, String firstName,
                          String lastName, String email, LocalDate dateAdded) {
        super(id, username, role);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateAdded = dateAdded;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public boolean isRequestedPasswordReset() {
        return requestedPasswordReset;
    }
    public void setRequestedPasswordReset(boolean requestedPasswordReset) {
        this.requestedPasswordReset = requestedPasswordReset;
    }

    public Integer getCoursesCount() {
        return coursesCount;
    }
    public void setCoursesCount(Integer coursesCount) {
        this.coursesCount = coursesCount;
    }
}