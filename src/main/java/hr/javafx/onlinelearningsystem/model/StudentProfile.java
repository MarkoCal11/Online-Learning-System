package hr.javafx.onlinelearningsystem.model;

import hr.javafx.onlinelearningsystem.enums.UserRole;

import java.time.LocalDate;

public final class StudentProfile extends UserProfile{

    private final String jmbag;

    public StudentProfile(String username,
                          String firstName, String lastName, String email,
                          LocalDate dateAdded, String jmbag) {
        super(username, UserRole.STUDENT, firstName, lastName, email, dateAdded);
        this.jmbag = jmbag;
    }

    public StudentProfile(Long id, String username,
                          String firstName, String lastName, String email,
                          LocalDate dateAdded, String jmbag) {
        super(id, username, UserRole.STUDENT, firstName, lastName, email, dateAdded);
        this.jmbag = jmbag;
    }

    public String getJmbag() {
        return jmbag;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName();
    }
}