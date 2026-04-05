package hr.javafx.onlinelearningsystem.controller.student;

import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Lesson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Window;

import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;

public class ViewLessonController {

    @FXML private Label lessonTitleLabel;
    @FXML private Label courseLabel;
    @FXML private TextArea contentArea;
    @FXML private Button closeButton;


    public void setLesson(Lesson lesson, Course course) {

        lessonTitleLabel.setText(lesson.getTitle());
        courseLabel.setText(course != null ? "Course: " + course.getTitle() : "Course");
        contentArea.setText(lesson.getContent());
        contentArea.positionCaret(0);
    }

    @FXML
    private void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }
}

