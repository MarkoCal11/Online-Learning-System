package hr.javafx.onlinelearningsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;

public class AdminMainLayoutController {

    @FXML
    private ScrollPane contentArea;

    private MenuController menuController;

    private Button currentActiveButton;

    @FXML
    private Button overviewButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button coursesButton;

    @FXML
    public void initialize() {
        menuController = new MenuController(contentArea);
        menuController.loadSection("/hr/javafx/onlinelearningsystem/fxml/admin/AdminOverview.fxml");

        setCurrentActiveButton(overviewButton);
    }

    private void setCurrentActiveButton(Button button) {
        if(currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        currentActiveButton = button;
    }

    @FXML
    private void returnToLogin() {
        menuController.logout();
    }

    @FXML
    private void loadAdminOverview() {
        menuController.loadSection("/hr/javafx/onlinelearningsystem/fxml/admin/AdminOverview.fxml");
        setCurrentActiveButton(overviewButton);
    }

    @FXML
    private void loadAdminUsers() {
        menuController.loadSection("/hr/javafx/onlinelearningsystem/fxml/admin/AdminUsers.fxml");
        setCurrentActiveButton(usersButton);
    }

    @FXML
    private void loadAdminCourses() {
        menuController.loadSection("/hr/javafx/onlinelearningsystem/fxml/admin/AdminCourses.fxml");
        setCurrentActiveButton(coursesButton);
    }
}
