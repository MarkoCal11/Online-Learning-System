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
import static hr.javafx.onlinelearningsystem.util.ValidationUtil.validateJmbag;

public class EditStudentController {

    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField jmbagField;
    @FXML private TextField emailField;

    @FXML private Button closeButton;

    private StudentProfile student;

    private List<TextField> studentDataFields;

    private final StudentRepository studentRepository = new StudentRepository();
    private final TeacherRepository teacherRepository = new TeacherRepository();

    public void setStudent(StudentProfile student) {
        this.student = student;

        usernameField.setText(student.getUsername());
        firstNameField.setText(student.getFirstName());
        lastNameField.setText(student.getLastName());
        jmbagField.setText(student.getJmbag());
        emailField.setText(student.getEmail());
    }

    @FXML
    private void initialize() {
        studentDataFields=List.of(usernameField, firstNameField,
                lastNameField, jmbagField, emailField);
    }

    @FXML
    private void handleSave() {

        if(!validateInput()){
            return;
        }

        StudentProfile studentProfile = new StudentProfile(
                usernameField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                student.getDateAdded(),
                jmbagField.getText().trim());

        try {
            UserService.getInstance().editStudent(studentProfile, student.getUsername());
            showInformation("Student edited successfully!");
            handleClose();
        } catch (IOException e) {
            showError("Failed to edit student: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean valid = true;
        boolean emptyFieldError = false;
        boolean emailError = false;
        boolean jmbagError = false;
        boolean usernameTaken = false;
        boolean jmbagTaken = false;
        boolean emailTaken = false;
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
            if(usernameField.getText().trim().equals(students.getUsername()) && !student.getUsername().equals(usernameField.getText().trim())) {
                usernameTaken = true;
                valid = false;
            }
            if(jmbagField.getText().trim().equals(students.getJmbag()) && !student.getJmbag().equals(jmbagField.getText().trim())) {
                jmbagTaken = true;
                valid = false;
            }
            if(emailField.getText().trim().equals(students.getEmail()) && !student.getEmail().equals(emailField.getText().trim())) {
                emailTaken = true;
                valid = false;
            }
        }
        for(TeacherProfile teachers : teacherRepository.findAll()) {
            if(usernameField.getText().trim().equals(teachers.getUsername()) && !student.getUsername().equals(usernameField.getText().trim())) {
                usernameTaken = true;
                valid = false;
            }
            if(emailField.getText().trim().equals(teachers.getEmail()) && !student.getEmail().equals(emailField.getText().trim())) {
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


        if(usernameTaken || jmbagTaken || emailTaken || emailError || jmbagError || emptyFieldError) {
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

    @FXML
    private void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }
}