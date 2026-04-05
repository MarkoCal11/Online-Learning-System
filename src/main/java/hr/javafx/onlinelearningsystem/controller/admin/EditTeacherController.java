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
import java.util.List;

import static hr.javafx.onlinelearningsystem.controller.LoginController.ERROR_STYLE;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.*;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;
import static hr.javafx.onlinelearningsystem.util.ValidationUtil.validateEmail;

public class EditTeacherController {

    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;

    @FXML private Button closeButton;

    private TeacherProfile teacher;

    private List<TextField> teacherDataFields;

    private final TeacherRepository teacherRepository = new TeacherRepository();
    private final StudentRepository studentRepository = new StudentRepository();

    public void setTeacher(TeacherProfile teacher) {
        this.teacher = teacher;

        usernameField.setText(teacher.getUsername());
        firstNameField.setText(teacher.getFirstName());
        lastNameField.setText(teacher.getLastName());
        emailField.setText(teacher.getEmail());
    }

    @FXML
    private void initialize() {
        teacherDataFields=List.of(usernameField, firstNameField,
                lastNameField, emailField);
    }

    @FXML
    private void handleSave() {

        if(!validateInput()){
            return;
        }

        TeacherProfile teacherProfile = new TeacherProfile(
                usernameField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                teacher.getDateAdded());

        try {
            UserService.getInstance().editTeacher(teacherProfile, teacher.getUsername());
            showInformation("Teacher edited successfully!");
            handleClose();
        } catch (IOException e) {
            showError("Failed to edit teacher: " + e.getMessage());
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
            if(usernameField.getText().trim().equals(teachers.getUsername()) && !teacher.getUsername().equals(usernameField.getText().trim())) {
                usernameTaken = true;
                valid = false;
            }
            if(emailField.getText().trim().equals(teachers.getEmail()) && !teacher.getEmail().equals(emailField.getText().trim())) {
                emailTaken = true;
                valid = false;
            }
        }
        for(StudentProfile students : studentRepository.findAll()) {
            if(usernameField.getText().trim().equals(students.getUsername()) && !teacher.getUsername().equals(usernameField.getText().trim())) {
                usernameTaken = true;
                valid = false;
            }
            if(emailField.getText().trim().equals(students.getEmail()) && !teacher.getEmail().equals(emailField.getText().trim())) {
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

        if(!validateEmailField(sb)) {
            emailError = true;
            valid = false;
        }

        if(usernameTaken || emailTaken || emailError || emptyFieldError) {
            showWarning(String.valueOf(sb));
        }

        return valid;
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

    @FXML
    private void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }
}
