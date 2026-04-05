package hr.javafx.onlinelearningsystem.model;

import hr.javafx.onlinelearningsystem.enums.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TeacherProfile extends UserProfile{

    public TeacherProfile(String username, String firstName,
                          String lastName, String email, LocalDate dateEmployed) {
        super(username, UserRole.TEACHER, firstName, lastName, email, dateEmployed);
    }

    public TeacherProfile(Long id, String username, String firstName,
                          String lastName, String email, LocalDate dateEmployed) {
        super(id, username, UserRole.TEACHER, firstName, lastName, email, dateEmployed);
    }
}
