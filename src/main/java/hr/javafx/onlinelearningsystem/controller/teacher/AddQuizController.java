package hr.javafx.onlinelearningsystem.controller.teacher;

import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Quiz;
import hr.javafx.onlinelearningsystem.repository.QuizRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static hr.javafx.onlinelearningsystem.controller.LoginController.ERROR_STYLE;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;

public class AddQuizController {

    @FXML private TextField titleField;
    @FXML private DatePicker dueDatePicker;
    @FXML private Button closeButton;

    private Course course;

    private final QuizRepository quizRepository = new QuizRepository();

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public void setCourse(Course course) {
        this.course = course;
    }

    @FXML
    private void initialize() {
        dueDatePicker.setEditable(false);
        dueDatePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DISPLAY_FORMAT.format(date);
            }
            @Override
            public LocalDate fromString(String text) {
                return dueDatePicker.getValue();
            }
        });
    }


    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        Quiz.Builder builder = new Quiz.Builder(null, titleField.getText().trim(), course);
        if (dueDatePicker.getValue() != null) {
            builder.onDate(dueDatePicker.getValue());
        }

        quizRepository.save(builder.build());
        showInformation("Quiz created successfully!");
        handleClose();
    }

    private boolean validateInput() {
        boolean valid = true;
        StringBuilder message = new StringBuilder();

        titleField.getStyleClass().remove(ERROR_STYLE);
        dueDatePicker.getEditor().getStyleClass().remove(ERROR_STYLE);

        if (titleField.getText().isBlank()) {
            titleField.getStyleClass().add(ERROR_STYLE);
            message.append("Please enter a quiz title.");
            valid = false;
        }

        if (valid) {
            String enteredTitle = titleField.getText().trim();
            for (Quiz quiz : quizRepository.findByCourse(course.getId())) {
                if (enteredTitle.equalsIgnoreCase(quiz.getTitle().trim())) {
                    titleField.getStyleClass().add(ERROR_STYLE);
                    message.append("Quiz with the same title already exists.");
                    valid = false;
                    break;
                }
            }
        }

        LocalDate selectedDueDate = dueDatePicker.getValue();
        if (selectedDueDate != null && selectedDueDate.isBefore(LocalDate.now())) {
            dueDatePicker.getEditor().getStyleClass().add(ERROR_STYLE);
            if (!message.isEmpty()) {
                message.append("\n");
            }
            message.append("Due date cannot be before today.");
            valid = false;
        }

        if (!valid) {
            showWarning(message.toString());
        }

        return valid;
    }

    @FXML
    private void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }
}

