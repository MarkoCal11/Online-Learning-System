package hr.javafx.onlinelearningsystem.controller.student;

import hr.javafx.onlinelearningsystem.enums.UserRole;
import hr.javafx.onlinelearningsystem.repository.PasswordChangeRepository;
import hr.javafx.onlinelearningsystem.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;

public class StudentSettingsController {

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

        if (passwordChangeRepository.hasPendingRequest(username, UserRole.STUDENT)) {
            showWarning("You already have a pending password change request.");
            refreshRequestState();
            return;
        }

        passwordChangeRepository.createRequest(username, UserRole.STUDENT);
        showInformation("Password change request sent. Please wait for admin approval.");
        refreshRequestState();
    }

    private void refreshRequestState() {
        String username = Session.getInstance().getCurrentUser().getUsername();
        boolean pending = passwordChangeRepository.hasPendingRequest(username, UserRole.STUDENT);

        requestStatusLabel.setText(pending
                ? "Request status: Pending admin review"
                : "Request status: No pending requests");
        requestButton.setDisable(pending);
    }
}

