package hr.javafx.onlinelearningsystem.controller.admin;

import javafx.scene.control.*;
import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.StudentProfile;
import hr.javafx.onlinelearningsystem.repository.EnrollmentRepository;
import hr.javafx.onlinelearningsystem.repository.StudentRepository;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.*;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;

public class ManageEnrollmentsController {

    @FXML private Label courseTitleLabel;
    @FXML private TableView<StudentProfile> enrolledStudentsList;
    @FXML private TableColumn<StudentProfile, String> nameColumn;
    @FXML private TableColumn<StudentProfile, String> usernameColumn;
    @FXML private TableColumn<StudentProfile, String> jmbagColumn;

    @FXML private Button closeButton;

    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
    private final StudentRepository studentRepository = new StudentRepository();

    private Course course;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        jmbagColumn.setCellValueFactory(new PropertyValueFactory<>("jmbag"));
    }

    public void setCourse(Course course) {
        this.course = course;
        courseTitleLabel.setText(course.getTitle());
        loadEnrolledStudents();
    }

    private void loadEnrolledStudents() {
        List<Long> enrolledIds = enrollmentRepository.findStudentIdsByCourse(course.getId());
        List<StudentProfile> enrolledStudents = studentRepository.findAll()
                .stream()
                .filter(e -> enrolledIds.contains(e.getId()))
                .toList();
        enrolledStudentsList.getItems().setAll(enrolledStudents);
    }

    @FXML
    private void handleAddStudent() {
        List<Long> enrolledIds = enrollmentRepository.findStudentIdsByCourse(course.getId());
        List<StudentProfile> available = studentRepository.findAll()
                .stream()
                .filter(s -> !enrolledIds.contains(s.getId()))
                .toList();

        if (available.isEmpty()) {
            showWarning("All students are already enrolled in this course.");
            return;
        }
        ChoiceDialog<StudentProfile> dialog = new ChoiceDialog<>(available.getFirst(), available);
        dialog.setTitle("Add Student");
        dialog.setHeaderText("Select a student to enroll in " + course.getTitle());
        dialog.setContentText("Student:");
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass()
                        .getResource("/hr/javafx/onlinelearningsystem/styles.css")).toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("card");
        dialog.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("primary-button");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("secondary-button");
        dialog.showAndWait().ifPresent(student -> {
            Optional<ButtonType> confirmation = showConfirmation(
                    "Add " + student.getFirstName() + " " + student.getLastName() + " to " + course.getTitle() + "?"
            );
            if (confirmation.isPresent() && confirmation.get() == ButtonType.OK) {
                enrollmentRepository.enroll(student.getId(), course.getId());
                showInformation(student.getFirstName() + " " + student.getLastName()
                        + " has been added to " + course.getTitle() + ".");
                loadEnrolledStudents();
            }
        });
    }

    @FXML
    private void handleRemoveStudent() {
        StudentProfile selectedStudent = enrolledStudentsList.getSelectionModel().getSelectedItem();
        if (Optional.ofNullable(selectedStudent).isEmpty()) {
            showWarning("Please select a student to remove.");
            return;
        }
        Optional<ButtonType> result = showConfirmation(
                "Remove " + selectedStudent.getFirstName() + " " + selectedStudent.getLastName()
                        + " from " + course.getTitle() + "?"
        );
        if (result.isPresent() && result.get() == ButtonType.OK) {
            enrollmentRepository.unenroll(selectedStudent.getId(), course.getId());
            showInformation(selectedStudent.getFirstName() + " " + selectedStudent.getLastName()
                    + " has been removed from " + course.getTitle() + ".");
            loadEnrolledStudents();
        }
    }

    @FXML
    private void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }
}
