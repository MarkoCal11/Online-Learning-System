package hr.javafx.onlinelearningsystem.controller;

import hr.javafx.onlinelearningsystem.auth.AuthResult;
import hr.javafx.onlinelearningsystem.auth.AuthenticationService;
import hr.javafx.onlinelearningsystem.exception.InvalidLoginCredentialsException;
import hr.javafx.onlinelearningsystem.model.UserProfile;
import hr.javafx.onlinelearningsystem.repository.StudentRepository;
import hr.javafx.onlinelearningsystem.repository.TeacherRepository;
import hr.javafx.onlinelearningsystem.util.SceneManager;
import hr.javafx.onlinelearningsystem.util.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.showError;

public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private static final double SCENE_SWITCH_DELAY_MS = 220;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private StackPane loadingPane;

    @FXML
    private Label loadingMessageLabel;

    AuthenticationService authService = new AuthenticationService();

    private final TeacherRepository teacherRepository = new TeacherRepository();
    private final StudentRepository studentRepository = new StudentRepository();

    public static final String ERROR_STYLE = "error-field";

    public void login() {
        if (!validateInput()) return;

        String username = usernameField.getText();
        String password = passwordField.getText();

        setLoadingState(true, "Checking credentials...");

        Thread.startVirtualThread(() -> {
            try {
                AuthResult result = authService.authenticate(username, password);
                UserProfile currentUser = switch (result.role()) {
                    case ADMIN -> null;
                    case TEACHER -> teacherRepository.findByUsername(result.username());
                    case STUDENT -> studentRepository.findByUsername(result.username());
                };

                Platform.runLater(() -> {
                    if (currentUser != null) Session.getInstance().setCurrentUser(currentUser);
                    loadingMessageLabel.setText("Login successful. Loading dashboard...");

                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.millis(SCENE_SWITCH_DELAY_MS),
                                    _ -> switchSceneByRole(result.role()))
                    );
                    timeline.play();
                });
            } catch (InvalidLoginCredentialsException e) {
                LOGGER.info("Login failed for user '{}': invalid credentials", username);
                Platform.runLater(() -> {
                    setLoadingState(false, "");
                    showError(e.getMessage());
                });
            } catch (Exception e) {
                LOGGER.error("Unexpected login failure for user '{}'", username, e);
                Platform.runLater(() -> {
                    setLoadingState(false, "");
                    showError("Login failed. Please try again.");
                });
            }
        });
    }

    private void switchSceneByRole(hr.javafx.onlinelearningsystem.enums.UserRole role) {
        switch (role) {
            case ADMIN -> SceneManager.showScene("/hr/javafx/onlinelearningsystem/fxml/admin/AdminMainLayout.fxml");
            case TEACHER -> SceneManager.showScene("/hr/javafx/onlinelearningsystem/fxml/teacher/TeacherMainLayout.fxml");
            case STUDENT -> SceneManager.showScene("/hr/javafx/onlinelearningsystem/fxml/student/StudentMainLayout.fxml");
        }
    }

    private void setLoadingState(boolean loading, String message) {
        loadingPane.setVisible(loading);
        loadingPane.setManaged(loading);
        loadingMessageLabel.setText(message);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        loginButton.setDisable(loading);
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
