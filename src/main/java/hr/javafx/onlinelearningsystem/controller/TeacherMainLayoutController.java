package hr.javafx.onlinelearningsystem.controller;

import hr.javafx.onlinelearningsystem.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

public class TeacherMainLayoutController {

    @FXML
    private ScrollPane contentArea;

    private MenuController menuController;

    private Button currentActiveButton;

    @FXML private Label welcomeLabel;
    @FXML private Button overviewButton;
    @FXML private Button coursesButton;
    @FXML private Button scheduleButton;
    @FXML private Button settingsButton;


    @FXML
    public void initialize() {
        menuController = new MenuController(contentArea);
        menuController.loadSection("/hr/javafx/onlinelearningsystem/fxml/teacher/TeacherOverview.fxml");

        String currentUsername = Session.getInstance().getCurrentUser().getUsername();
        welcomeLabel.setText("Welcome, " + currentUsername);
        setActiveButton(overviewButton);
    }

    private void setActiveButton(Button button) {
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
    private void loadTeacherOverview() {
        menuController.loadSection("/hr/javafx/onlinelearningsystem/fxml/teacher/TeacherOverview.fxml");
        setActiveButton(overviewButton);
    }

    @FXML
    private void loadTeacherCourses() {
        menuController.loadSection("/hr/javafx/onlinelearningsystem/fxml/teacher/TeacherCourses.fxml");
        setActiveButton(coursesButton);
    }

    @FXML
    private void loadTeacherSchedule() {
        menuController.loadSection("/hr/javafx/onlinelearningsystem/fxml/teacher/TeacherSchedule.fxml");
        setActiveButton(scheduleButton);
    }

    @FXML
    private void loadTeacherSettings() {
        menuController.loadSection("/hr/javafx/onlinelearningsystem/fxml/teacher/TeacherSettings.fxml");
        setActiveButton(settingsButton);
    }
}