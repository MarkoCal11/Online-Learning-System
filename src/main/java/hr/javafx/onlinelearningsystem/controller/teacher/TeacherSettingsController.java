package hr.javafx.onlinelearningsystem.controller.teacher;

import hr.javafx.onlinelearningsystem.enums.UserRole;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.repository.PasswordChangeRepository;
import hr.javafx.onlinelearningsystem.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showError;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;

public class TeacherSettingsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherSettingsController.class);

    @FXML private Label requestStatusLabel;
    @FXML private Button requestButton;

    private final PasswordChangeRepository passwordChangeRepository = new PasswordChangeRepository();

    @FXML
    private void initialize() {
        refreshRequestState();
    }

    @FXML
    private void handleRequestPasswordChange() {
        String username = Session.getInstance().getCurrentUser().getUsername();

        try {
            if (passwordChangeRepository.hasPendingRequest(username, UserRole.TEACHER)) {
                showWarning("You already have a pending password change request.");
                refreshRequestState();
                return;
            }

            passwordChangeRepository.createRequest(username, UserRole.TEACHER);
            showInformation("Password change request sent. Please wait for admin approval.");
            refreshRequestState();
        } catch (RepositoryException e) {
            LOGGER.error("Failed to create password change request for teacher '{}'", username, e);
            showError("Failed to send teacher password-change request: " + e.getMessage());
        }
    }

    private void refreshRequestState() {
        try {
            String username = Session.getInstance().getCurrentUser().getUsername();
            boolean pending = passwordChangeRepository.hasPendingRequest(username, UserRole.TEACHER);

            requestStatusLabel.setText(pending
                    ? "Request status: Pending admin review"
                    : "Request status: No pending requests");
            requestButton.setDisable(pending);
        } catch (RepositoryException e) {
            LOGGER.error("Failed to load password change request state for teacher", e);
            requestStatusLabel.setText("Request status: Unavailable");
            requestButton.setDisable(false);
            showError("Failed to load teacher request status: " + e.getMessage());
        }
    }
}

