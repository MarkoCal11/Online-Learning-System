package hr.javafx.onlinelearningsystem.controller;

import hr.javafx.onlinelearningsystem.auth.AuthResult;
import hr.javafx.onlinelearningsystem.auth.AuthenticationService;
import hr.javafx.onlinelearningsystem.exception.InvalidLoginCredentialsException;
import hr.javafx.onlinelearningsystem.model.UserProfile;
import hr.javafx.onlinelearningsystem.repository.StudentRepository;
import hr.javafx.onlinelearningsystem.repository.TeacherRepository;
import hr.javafx.onlinelearningsystem.util.SceneManager;
import hr.javafx.onlinelearningsystem.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.showError;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;

public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    AuthenticationService authService = new AuthenticationService();

    private final TeacherRepository teacherRepository = new TeacherRepository();
    private final StudentRepository studentRepository = new StudentRepository();

    public static final String ERROR_STYLE = "error-field";

    public void login(){
        String username = usernameField.getText();
        String password = passwordField.getText();

        if(!validateInput()) {
            return;
        }


        try {
            AuthResult result = authService.authenticate(username, password);

            showInformation("Logged in successfully!");

            UserProfile currentUser = switch (result.role()) {
                case ADMIN -> null;
                case TEACHER -> teacherRepository.findByUsername(result.username());
                case STUDENT -> studentRepository.findByUsername(result.username());
            };

            if(currentUser != null) {
                Session.getInstance().setCurrentUser(currentUser);
            }

            switch (result.role()) {
                case ADMIN -> SceneManager.showScene("/hr/javafx/onlinelearningsystem/fxml/admin/AdminMainLayout.fxml");
                case TEACHER -> SceneManager.showScene("/hr/javafx/onlinelearningsystem/fxml/teacher/TeacherMainLayout.fxml");
                case STUDENT -> SceneManager.showScene("/hr/javafx/onlinelearningsystem/fxml/student/StudentMainLayout.fxml");
            }
        } catch (InvalidLoginCredentialsException e) {
            showError(e.getMessage());
            passwordField.clear();
            passwordField.getStyleClass().add(ERROR_STYLE);
        } catch (IOException e) {
            LOGGER.error("Failed to load post-login scene for user '{}'", username, e);
            showError("Unable to load the selected screen. Please try again.");
        }
    }

    private boolean validateInput(){
        usernameField.getStyleClass().remove(ERROR_STYLE);

        if(usernameField.getText().isBlank() && passwordField.getText().isBlank()){
            showError("Please enter your credentials");
            usernameField.getStyleClass().add(ERROR_STYLE);
            passwordField.getStyleClass().add(ERROR_STYLE);
            return false;
        }
        else if(usernameField.getText().isBlank()){
            showError("Username field is empty");
            usernameField.getStyleClass().add(ERROR_STYLE);
            return false;
        }
        else if(passwordField.getText().isBlank()) {
            showError("Password field is empty");
            passwordField.getStyleClass().add(ERROR_STYLE);
            return false;
        }

        return true;
    }

}