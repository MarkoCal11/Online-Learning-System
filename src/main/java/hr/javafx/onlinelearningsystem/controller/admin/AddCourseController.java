package hr.javafx.onlinelearningsystem.controller.admin;

import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.repository.CourseRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import java.util.List;

import static hr.javafx.onlinelearningsystem.controller.LoginController.ERROR_STYLE;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;

public class AddCourseController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private Spinner<Integer> ectsSpinner;

    @FXML private Button closeButton;

    private List<TextField> courseDataFields;

    private final CourseRepository courseRepository = new CourseRepository();

    @FXML
    public void initialize() {
        courseDataFields=List.of(titleField, descriptionField);
        SpinnerValueFactory<Integer> spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,30,5);
        ectsSpinner.setValueFactory(spinnerValueFactory);
    }

    @FXML
    private void handleSave() {
        if(!validateInput()){
            return;
        }
        Course course = new Course(
                titleField.getText().trim(),
                descriptionField.getText(),
                ectsSpinner.getValue(),
                null);
        courseRepository.save(course);
        showInformation("Course added successfully!");
        handleClose();
    }

    private boolean validateInput() {
        boolean valid = true;
        boolean emptyFieldError = false;
        boolean titleTaken = false;
        StringBuilder sb = new StringBuilder();

        for(TextField field : courseDataFields) {
            field.getStyleClass().remove(ERROR_STYLE);
            if(field.getText().isBlank()) {
                field.getStyleClass().add(ERROR_STYLE);
                emptyFieldError = true;
                valid = false;
            }
        }

        if(emptyFieldError) {
            sb.append("Please fill every box.");
        }

        for(Course courses : courseRepository.findAll()) {
            if(titleField.getText().trim().equalsIgnoreCase(courses.getTitle())) {
                titleTaken = true;
                valid = false;
            }
        }

        if(titleTaken) {
            titleField.getStyleClass().add(ERROR_STYLE);
            sb.append("\n ").append("Course with the same title already exists.");
        }

        if(emptyFieldError || titleTaken) {
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
