package hr.javafx.onlinelearningsystem.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Objects;
import java.util.Optional;

public class AlertUtil {

    private static final String STYLESHEET = Objects.requireNonNull(AlertUtil.class
                    .getResource("/hr/javafx/onlinelearningsystem/styles.css"))
            .toExternalForm();

    private static final String PRIMARY_BUTTON = "primary-button";
    private static final String SECONDARY_BUTTON = "secondary-button";

    private AlertUtil() {}

    private static void applyStyle(Alert alert) {
        alert.getDialogPane().getStylesheets().add(STYLESHEET);
        alert.getDialogPane().getStyleClass().add("card");
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyStyle(alert);
        alert.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("danger-button");
        alert.showAndWait();
    }

    public static void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyStyle(alert);
        alert.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add(SECONDARY_BUTTON);
        alert.showAndWait();
    }

    public static void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyStyle(alert);
        alert.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add(PRIMARY_BUTTON);
        alert.showAndWait();
    }

    public static Optional<ButtonType> showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyStyle(alert);
        alert.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add(SECONDARY_BUTTON);
        alert.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add(PRIMARY_BUTTON);
        return alert.showAndWait();
    }
}
