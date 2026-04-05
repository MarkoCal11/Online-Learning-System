package hr.javafx.onlinelearningsystem.controller.admin;

import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.TeacherProfile;
import hr.javafx.onlinelearningsystem.repository.CourseRepository;
import hr.javafx.onlinelearningsystem.util.SceneManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.*;

public class AdminCoursesController {

    @FXML private Button addCourseButton;
    @FXML private Button editCourseButton;
    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, String> courseIdColumn;
    @FXML private TableColumn<Course, String> courseNameColumn;
    @FXML private TableColumn<Course, String> courseDescriptionColumn;
    @FXML private TableColumn<Course, String> teacherColumn;
    @FXML private TableColumn<Course, Integer> courseECTSColumn;
    @FXML private TableColumn<Course, Integer> studentsCountColumn;
    @FXML private TableColumn<Course, Integer> lessonsCountColumn;
    @FXML private TableColumn<Course, Integer> quizzesCountColumn;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalEnrollmentsLabel;

    private final CourseRepository courseRepository = new CourseRepository();
    private final ObservableList<Course> coursesData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTables();
        loadCourses();
        coursesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        totalCoursesLabel.setText(String.valueOf(courseRepository.countCourses()));
        totalEnrollmentsLabel.setText(String.valueOf(courseRepository.countEnrollments()));
    }

    @FXML
    private void handleAddCourse() {
        Window owner = addCourseButton.getScene().getWindow();
        SceneManager.showPopup("/hr/javafx/onlinelearningsystem/fxml/admin/AddCourse.fxml", owner);
        loadCourses();
        totalCoursesLabel.setText(String.valueOf(courseRepository.countCourses()));
        totalEnrollmentsLabel.setText(String.valueOf(courseRepository.countEnrollments()));
    }

    @FXML
    private void handleEditCourse() {
        Course selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
        if(Optional.ofNullable(selectedCourse).isEmpty()) {
            showWarning("Please first select a course you want to edit");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(
                    "/hr/javafx/onlinelearningsystem/fxml/admin/EditCourse.fxml"
            ));
            Parent root = loader.load();
            EditCourseController controller = loader.getController();
            controller.setCourse(selectedCourse);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Online Learning System");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(editCourseButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            loadCourses();
        } catch (IOException e) {
            showError("Failed to open edit screen" + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveCourse() {
        Course selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
        if(Optional.ofNullable(selectedCourse).isEmpty()) {
            showWarning("Please first select a course you want to remove");
            return;
        }
        Optional<ButtonType> result = showConfirmation("Are you sure you want to remove this course?");

        if(result.isPresent() && result.get() == ButtonType.OK) {
            try {
                courseRepository.delete(selectedCourse.getId());
                loadCourses();
                totalCoursesLabel.setText(String.valueOf(courseRepository.countCourses()));
                totalEnrollmentsLabel.setText(String.valueOf(courseRepository.countEnrollments()));
                showInformation("Course successfully removed.");
            } catch (RepositoryException e) {
                showError("Failed to remove course: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleManageStudents() {
        Course selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
        if (Optional.ofNullable(selectedCourse).isEmpty()) {
            showWarning("Please first select a course.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(
                    "/hr/javafx/onlinelearningsystem/fxml/admin/ManageEnrollments.fxml"
            ));
            Parent root = loader.load();
            ManageEnrollmentsController controller = loader.getController();
            controller.setCourse(selectedCourse);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Manage Enrollments");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(coursesTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            loadCourses();
            totalEnrollmentsLabel.setText(String.valueOf(courseRepository.countEnrollments()));
        } catch (IOException e) {
            showError("Failed to open enrollment screen: " + e.getMessage());
        }
    }

    private void loadCourses() {
        coursesData.clear();
        coursesData.addAll(courseRepository.findAll());
    }

    private void setupTables() {
        courseIdColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getId()).asString());
        courseNameColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTitle()));
        courseDescriptionColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDescription()));
        teacherColumn.setCellValueFactory(cellData -> {
            TeacherProfile teacher = cellData.getValue().getTeacher();
            return new SimpleObjectProperty<>(teacher == null ? "VACANT" :
                            teacher.getFirstName() + " " + teacher.getLastName());
        });
        courseECTSColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getEcts()));
        studentsCountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getStudentCount()));
        lessonsCountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getLessonsCount()));
        quizzesCountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getQuizzesCount()));
        coursesTable.setItems(coursesData);
    }
}