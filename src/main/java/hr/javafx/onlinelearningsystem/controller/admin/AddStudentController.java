package hr.javafx.onlinelearningsystem.controller.admin;

import hr.javafx.onlinelearningsystem.exception.UserDataFormatException;
import hr.javafx.onlinelearningsystem.model.StudentProfile;
import hr.javafx.onlinelearningsystem.model.TeacherProfile;
import hr.javafx.onlinelearningsystem.repository.StudentRepository;
import hr.javafx.onlinelearningsystem.repository.TeacherRepository;
import hr.javafx.onlinelearningsystem.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static hr.javafx.onlinelearningsystem.controller.LoginController.ERROR_STYLE;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.*;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;
import static hr.javafx.onlinelearningsystem.util.ValidationUtil.validateEmail;
import static hr.javafx.onlinelearningsystem.util.ValidationUtil.validateJmbag;

public class AddStudentController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField jmbagField;
    @FXML private TextField emailField;

    @FXML private Button closeButton;

    private List<TextField> studentDataFields;

    private final StudentRepository studentRepository = new StudentRepository();
    private final TeacherRepository teacherRepository = new TeacherRepository();

    @FXML
    public void initialize() {
        studentDataFields=List.of(usernameField, passwordField, firstNameField,
                            lastNameField, jmbagField, emailField);
    }

    @FXML
    private void handleSave() {

        if(!validateInput()){
            return;
        }

        StudentProfile student = new StudentProfile(
                usernameField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                LocalDate.now(),
                jmbagField.getText().trim());

        try {
            UserService.getInstance().addStudent(student, passwordField.getText());
            showInformation("Student added successfully!");
            handleClose();
        } catch (IOException e) {
            showError("Failed to add student: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean valid = true;
        boolean emptyFieldError = false;
        boolean emailError = false;
        boolean jmbagError = false;
        boolean usernameTaken = false;
        boolean emailTaken = false;
        boolean jmbagTaken = false;
        StringBuilder sb = new StringBuilder();



        for(TextField field : studentDataFields) {
            field.getStyleClass().remove(ERROR_STYLE);
            if(field.getText().isBlank()) {
                field.getStyleClass().add(ERROR_STYLE);
                emptyFieldError = true;
                valid = false;
            }
        }

        if(emptyFieldError) {
            sb.append("Please fill every box.");
        }

        for(StudentProfile students : studentRepository.findAll()) {
            if(usernameField.getText().trim().equals(students.getUsername())) {
                usernameTaken = true;
                valid = false;
            }
            if(jmbagField.getText().trim().equals(students.getJmbag())) {
                jmbagTaken = true;
                valid = false;
            }
            if(emailField.getText().trim().equals(students.getEmail())) {
                emailTaken = true;
                valid = false;
            }
        }
        for(TeacherProfile teachers : teacherRepository.findAll()) {
            if (usernameField.getText().trim().equals(teachers.getUsername())) {
                usernameTaken = true;
                valid = false;
            }
            if (emailField.getText().trim().equals(teachers.getEmail())) {
                emailTaken = true;
                valid = false;
            }
        }

        if(usernameTaken) {
            usernameField.getStyleClass().add(ERROR_STYLE);
            sb.append("\n ").append("Username already taken.");
        }

        if(jmbagTaken) {
            jmbagField.getStyleClass().add(ERROR_STYLE);
            sb.append("\n ").append("JMBAG already taken.");
        }
        if(emailTaken) {
            emailField.getStyleClass().add(ERROR_STYLE);
            sb.append("\n ").append("Email already taken.");
        }

        if(!validateJmbagField(sb)) {
            jmbagError = true;
            valid = false;
        }

        if(!validateEmailField(sb)) {
            emailError = true;
            valid = false;
        }


        if(emptyFieldError || usernameTaken || jmbagTaken || emailTaken || emailError || jmbagError) {
            showWarning(String.valueOf(sb));
        }

        return valid;
    }

    private boolean validateJmbagField(StringBuilder sb) {
        try {
            validateJmbag(jmbagField.getText());
            return true;
        } catch (UserDataFormatException e) {
            sb.append("\n ").append(e.getMessage());
            jmbagField.getStyleClass().add(ERROR_STYLE);
            return false;
        }
    }

    private boolean validateEmailField(StringBuilder sb) {
        try {
            validateEmail(emailField.getText());
            return true;
        } catch (UserDataFormatException e) {
            sb.append("\n ").append(e.getMessage());
            emailField.getStyleClass().add(ERROR_STYLE);
            return false;
        }
    }

    public void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }
}