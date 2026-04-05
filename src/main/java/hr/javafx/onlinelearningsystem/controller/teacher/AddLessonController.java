package hr.javafx.onlinelearningsystem.controller.teacher;

import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Lesson;
import hr.javafx.onlinelearningsystem.repository.LessonRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import static hr.javafx.onlinelearningsystem.controller.LoginController.ERROR_STYLE;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;


public class AddLessonController {

    @FXML private TextField titleField;
    @FXML private TextArea contentArea;

    @FXML private Button closeButton;

    private Course course;

    private final LessonRepository lessonRepository = new LessonRepository();

    public void setCourse(Course course) {
        this.course = course;
    }

    @FXML
    private void handleSave() {

        if(!validateInput()) {
            return;
        }

        Lesson lesson = new Lesson(
                titleField.getText().trim(),
                contentArea.getText(),
                course
        );
        lessonRepository.save(lesson);
        showInformation("Lesson created successfully!");
        handleClose();
    }

    private boolean validateInput() {
        boolean valid = true;
        boolean emptyFieldError = false;
        boolean titleTaken = false;
        boolean emptyContentError = false;
        StringBuilder sb = new StringBuilder();

        titleField.getStyleClass().remove(ERROR_STYLE);
        contentArea.getStyleClass().remove(ERROR_STYLE);

        if(titleField.getText().isBlank()) {
            titleField.getStyleClass().add(ERROR_STYLE);
            sb.append("Please enter a title.");
            emptyFieldError = true;
            valid = false;
        }

        for(Lesson lessons: lessonRepository.findByCourse(course.getId())) {
            if(titleField.getText().trim().equalsIgnoreCase(lessons.getTitle())) {
                titleField.getStyleClass().add(ERROR_STYLE);
                sb.append("\n ").append("Lesson with same title already exists.");
                titleTaken = true;
                valid = false;
            }
        }

        if(contentArea.getText().isBlank()) {
            contentArea.getStyleClass().add(ERROR_STYLE);
            sb.append("\n ").append("Please enter lesson content.");
            emptyContentError = true;
            valid = false;
        }

        if(emptyFieldError || titleTaken || emptyContentError) {
            showWarning(String.valueOf(sb));
        }

        return valid;
    }

    @FXML
    private void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }
}
