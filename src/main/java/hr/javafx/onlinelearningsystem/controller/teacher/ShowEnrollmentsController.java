package hr.javafx.onlinelearningsystem.controller.teacher;

import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Enrolment;
import hr.javafx.onlinelearningsystem.repository.EnrollmentRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.showError;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;

public class ShowEnrollmentsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowEnrollmentsController.class);

    @FXML private Label courseTitleLabel;
    @FXML private TableView<Enrolment> enrolledStudentsList;
    @FXML private TableColumn<Enrolment, String> nameColumn;
    @FXML private TableColumn<Enrolment, String> usernameColumn;
    @FXML private TableColumn<Enrolment, String> jmbagColumn;
    @FXML private TableColumn<Enrolment, String> statusColumn;
    @FXML private TableColumn<Enrolment, String> finalGradeColumn;

    @FXML private Button closeButton;

    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();

    private Course course;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        cellData.getValue().getStudent().getFirstName() + " " + cellData.getValue().getStudent().getLastName()
                ));
        usernameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStudent().getUsername()));
        jmbagColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStudent().getJmbag()));
        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getEnrollmentStatus().name()));
        finalGradeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        cellData.getValue().getFinalGrade().map(Object::toString).orElse("-")
                ));
    }

    public void setCourse(Course course) {
        this.course = course;
        courseTitleLabel.setText(course.getTitle());
        loadEnrolledStudents();
    }

    private void loadEnrolledStudents() {
        List<Enrolment> enrolledStudents = enrollmentRepository.findAll()
                .stream()
                .filter(enrolment -> enrolment.getCourse() != null)
                .filter(enrolment -> course.getId().equals(enrolment.getCourse().getId()))
                .toList();
        enrolledStudentsList.getItems().setAll(enrolledStudents);
    }

    @FXML
    private void handleSetGrade() {
        Enrolment selectedEnrolment = enrolledStudentsList.getSelectionModel().getSelectedItem();
        if (selectedEnrolment == null) {
            showWarning("Please select a student first.");
            return;
        }

        TextInputDialog gradeDialog = new TextInputDialog(
                selectedEnrolment.getFinalGrade().map(String::valueOf).orElse("")
        );
        gradeDialog.setTitle("Set Final Grade");
        gradeDialog.setHeaderText("Assign final grade for "
                + selectedEnrolment.getStudent().getFirstName() + " "
                + selectedEnrolment.getStudent().getLastName());
        gradeDialog.setContentText("Final grade (1 - 5):");
        styleGradeDialog(gradeDialog);

        Optional<String> result = gradeDialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String value = result.get().trim();
        if (value.isEmpty()) {
            showWarning("Grade cannot be empty.");
            return;
        }

        try {
            double grade = Double.parseDouble(value);
            if (grade < 1.0 || grade > 5.0) {
                showWarning("Final grade must be between 1.0 and 5.0.");
                return;
            }

            enrollmentRepository.updateFinalGrade(selectedEnrolment.getId(), grade);
            showInformation("Final grade saved.");
            loadEnrolledStudents();
        } catch (NumberFormatException _) {
            LOGGER.warn("Invalid final grade input '{}' for enrollment {}", value, selectedEnrolment.getId());
            showError("Please enter a valid number.");
        }
    }

    @FXML
    private void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }

    private void styleGradeDialog(TextInputDialog dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.setPrefWidth(540);
        pane.setMinWidth(520);
        pane.getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource("/hr/javafx/onlinelearningsystem/styles.css")).toExternalForm());
        pane.getStyleClass().add("grade-dialog");

        if (pane.lookupButton(ButtonType.OK) != null) {
            pane.lookupButton(ButtonType.OK).getStyleClass().add("primary-button");
        }
        if (pane.lookupButton(ButtonType.CANCEL) != null) {
            pane.lookupButton(ButtonType.CANCEL).getStyleClass().add("secondary-button");
        }

        if (dialog.getEditor() != null) {
            dialog.getEditor().getStyleClass().add("login-field");
        }
    }
}
