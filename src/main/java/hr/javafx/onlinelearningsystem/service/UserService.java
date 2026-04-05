package hr.javafx.onlinelearningsystem.service;

import hr.javafx.onlinelearningsystem.auth.UserCredentialsService;
import hr.javafx.onlinelearningsystem.enums.UserRole;
import hr.javafx.onlinelearningsystem.model.StudentProfile;
import hr.javafx.onlinelearningsystem.model.TeacherProfile;
import hr.javafx.onlinelearningsystem.repository.PasswordChangeRepository;
import hr.javafx.onlinelearningsystem.repository.StudentRepository;
import hr.javafx.onlinelearningsystem.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordChangeRepository passwordChangeRepository;
    private final UserCredentialsService userCredentialsService;
    private static UserService instance;

    public UserService(StudentRepository studentRepository, TeacherRepository teacherRepository,
                       PasswordChangeRepository passwordChangeRepository, UserCredentialsService userCredentialsService) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.passwordChangeRepository = passwordChangeRepository;
        this.userCredentialsService = userCredentialsService;
    }

    public static UserService getInstance() {
        if(Optional.ofNullable(instance).isEmpty()) {
            instance = new UserService(
                    new StudentRepository(),
                    new TeacherRepository(),
                    new PasswordChangeRepository(),
                    new UserCredentialsService()
            );
        }
        return instance;
    }

    public void addStudent(StudentProfile student, String password) throws IOException {
        studentRepository.save(student);
        try{
            userCredentialsService.addUserToFile(student.getUsername(), password, UserRole.STUDENT);
        } catch (IOException e) {
            LOGGER.error("Student '{}' saved to DB but credentials write failed", student.getUsername(), e);
            throw new IOException("Student saved to database, but failure when writing credentials to txt. file.");
        }
    }

    public void addTeacher(TeacherProfile teacher, String password) throws IOException {
        teacherRepository.save(teacher);
        try{
            userCredentialsService.addUserToFile(teacher.getUsername(), password, UserRole.TEACHER);
        } catch (IOException e) {
            LOGGER.error("Teacher '{}' saved to DB but credentials write failed", teacher.getUsername(), e);
            throw new IOException("Teacher saved to database, but failure when writing credentials to txt. file.");
        }
    }

    public void removeStudent(String username) throws IOException {
        passwordChangeRepository.deleteRequestsForUser(username, UserRole.STUDENT);
        studentRepository.delete(username);
        try{
            userCredentialsService.removeUserFromFile(username);
        } catch (IOException e) {
            LOGGER.error("Student '{}' deleted from DB but credentials removal failed", username, e);
            throw new IOException("Student deleted from database, but failure when deleting credentials from txt. file.");
        }
    }

    public void removeTeacher(String username) throws IOException {
        passwordChangeRepository.deleteRequestsForUser(username, UserRole.TEACHER);
        teacherRepository.delete(username);
        try{
            userCredentialsService.removeUserFromFile(username);
        } catch (IOException e) {
            LOGGER.error("Teacher '{}' deleted from DB but credentials removal failed", username, e);
            throw new IOException("Teacher deleted from database, but failure when deleting credentials from txt. file.");
        }
    }

    public void editStudent(StudentProfile student, String oldUsername) throws IOException {
        studentRepository.update(student, oldUsername);
        passwordChangeRepository.updateUsernameInRequests(oldUsername, student.getUsername(), UserRole.STUDENT);
        try{
            userCredentialsService.updateUsernameInFile(oldUsername, student.getUsername());
        } catch (IOException e) {
            LOGGER.error("Student username update '{}' -> '{}' failed in credentials file", oldUsername, student.getUsername(), e);
            throw new IOException("Student updated in database, but failure when updating credentials in txt. file.");
        }
    }

    public void editTeacher(TeacherProfile teacher, String oldUsername) throws IOException {
        teacherRepository.update(teacher, oldUsername);
        passwordChangeRepository.updateUsernameInRequests(oldUsername, teacher.getUsername(), UserRole.TEACHER);
        try{
            userCredentialsService.updateUsernameInFile(oldUsername, teacher.getUsername());
        } catch (IOException e) {
            LOGGER.error("Teacher username update '{}' -> '{}' failed in credentials file", oldUsername, teacher.getUsername(), e);
            throw new IOException("Teacher updated in database, but failure when updating credentials in txt. file.");
        }
    }

    public void updatePassword(String username, String newPassword) throws IOException {
        try {
            userCredentialsService.updatePasswordInFile(username, newPassword);
        } catch (IOException e) {
            LOGGER.error("Password update failed in credentials file for user '{}'", username, e);
            throw new IOException("Failed to update password.");
        }
    }
}