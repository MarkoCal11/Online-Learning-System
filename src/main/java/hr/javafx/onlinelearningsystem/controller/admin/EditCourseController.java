package hr.javafx.onlinelearningsystem.controller.admin;

import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.TeacherProfile;
import hr.javafx.onlinelearningsystem.repository.CourseRepository;
import hr.javafx.onlinelearningsystem.repository.TeacherRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Window;

import java.util.List;

import static hr.javafx.onlinelearningsystem.controller.LoginController.ERROR_STYLE;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;

public class EditCourseController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private Spinner<Integer> ectsSpinner;
    @FXML private ComboBox<String> teacherComboBox;

    @FXML private Button closeButton;

    private List<TextField> courseDataFields;

    private Course course;
    private final CourseRepository courseRepository = new CourseRepository();
    private final TeacherRepository teacherRepository = new TeacherRepository();

    @FXML
    private void initialize() {
        courseDataFields= List.of(titleField, descriptionField);
    }

    public void setCourse(Course course) {
        this.course = course;

        titleField.setText(course.getTitle());
        descriptionField.setText(course.getDescription());
        SpinnerValueFactory<Integer> spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,30, course.getEcts());
        ectsSpinner.setValueFactory(spinnerValueFactory);
        ObservableList<TeacherProfile> teachers = FXCollections.observableArrayList(teacherRepository.findAll());
        teacherComboBox.getItems().clear();
        teacherComboBox.getItems().add("VACANT");
        teacherComboBox.getItems().addAll(teachers.stream().map(TeacherProfile::getUsername).toList());
    }

    @FXML
    private void handleSave() {
        if(!validateInput()){
            return;
        }
        TeacherProfile teacher = course.getTeacher();
        String selectedTeacher = teacherComboBox.getValue();
        if (selectedTeacher != null) {
            if (selectedTeacher.equals("VACANT")) {
                teacher = null;
            } else {
                teacher = teacherRepository.findByUsername(selectedTeacher);
            }
            Course courseForTeacherUpdate = new Course(
                    titleField.getText(),
                    descriptionField.getText(),
                    ectsSpinner.getValue(),
                    teacher);
            courseRepository.addTeacherToCourse(courseForTeacherUpdate, teacher);
        }

        Course editedCourse = new Course(
                titleField.getText(),
                descriptionField.getText(),
                ectsSpinner.getValue(),
                teacher);
        courseRepository.update(editedCourse, course.getTitle());
        showInformation("Course edited successfully!");
        handleClose();
    }

    private boolean validateInput() {
        boolean valid = true;
        boolean titleTaken = false;
        boolean emptyFieldError = false;
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
            if(titleField.getText().equals(courses.getTitle()) && !course.getTitle().equals(titleField.getText())) {
                titleTaken = true;
                valid = false;
            }
        }

        if(titleTaken) {
            titleField.getStyleClass().add(ERROR_STYLE);
            sb.append("\n ").append("Course with the same title already exists.");
        }

        if(titleTaken || emptyFieldError) {
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
