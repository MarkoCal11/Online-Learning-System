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

public class AddTeacherController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;

    @FXML private Button closeButton;

    private List<TextField> teacherDataFields;

    private final TeacherRepository teacherRepository = new TeacherRepository();
    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        teacherDataFields=List.of(usernameField, passwordField, firstNameField,
                lastNameField, emailField);
    }

    @FXML
    private void handleSave() {
        if(!validateInput()){
            return;
        }

        TeacherProfile teacher = new TeacherProfile(
                usernameField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                LocalDate.now());

        try {
            UserService.getInstance().addTeacher(teacher, passwordField.getText());
            showInformation("Teacher added successfully!");
            handleClose();
        } catch (IOException e) {
            showError("Failed to add teacher: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean valid = true;
        boolean emptyFieldError = false;
        boolean emailError = false;
        boolean usernameTaken = false;
        boolean emailTaken = false;
        StringBuilder sb = new StringBuilder();



        for(TextField field : teacherDataFields) {
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

        for(TeacherProfile teachers : teacherRepository.findAll()) {
            if(usernameField.getText().trim().equals(teachers.getUsername())) {
                usernameTaken = true;
                valid = false;
            }
            if(emailField.getText().trim().equals(teachers.getEmail())) {
                emailTaken = true;
                valid = false;
            }
        }
        for(StudentProfile students : studentRepository.findAll()) {
            if(usernameField.getText().trim().equals(students.getUsername())) {
                usernameTaken = true;
                valid = false;
            }
            if(emailField.getText().trim().equals(students.getEmail())) {
                emailTaken = true;
                valid = false;
            }
        }

        if(usernameTaken) {
            usernameField.getStyleClass().add(ERROR_STYLE);
            sb.append("\n ").append("Username already taken.");
        }
        if(emailTaken) {
            emailField.getStyleClass().add(ERROR_STYLE);
            sb.append("\n ").append("Email already taken.");
        }


        try {
            validateEmail(emailField.getText());
        } catch (UserDataFormatException e) {
            sb.append("\n ").append(e.getMessage());
            emailField.getStyleClass().add(ERROR_STYLE);
            emailError = true;
            valid = false;
        }

        if(emptyFieldError || usernameTaken || emailTaken || emailError) {
            showWarning(String.valueOf(sb));
        }

        return valid;
    }

    public void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }
}
