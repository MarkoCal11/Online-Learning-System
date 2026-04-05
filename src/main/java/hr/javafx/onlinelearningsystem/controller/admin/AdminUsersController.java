package hr.javafx.onlinelearningsystem.controller.admin;

import hr.javafx.onlinelearningsystem.enums.UserRole;
import hr.javafx.onlinelearningsystem.repository.PasswordChangeRepository;
import hr.javafx.onlinelearningsystem.repository.EnrollmentRepository;
import javafx.scene.control.*;
import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.StudentProfile;
import hr.javafx.onlinelearningsystem.model.TeacherProfile;
import hr.javafx.onlinelearningsystem.repository.CourseRepository;
import hr.javafx.onlinelearningsystem.repository.StudentRepository;
import hr.javafx.onlinelearningsystem.repository.TeacherRepository;
import hr.javafx.onlinelearningsystem.service.UserService;
import hr.javafx.onlinelearningsystem.util.SceneManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.*;

public class AdminUsersController {

    @FXML private Button addStudentButton;
    @FXML private Button editStudentButton;
    @FXML private Button addTeacherButton;
    @FXML private Button editTeacherButton;
    @FXML private TableView<StudentProfile> studentProfileTable;
    @FXML private TableView<TeacherProfile> teacherProfileTable;
    @FXML private TableColumn<StudentProfile, String> studentJmbagColumn;
    @FXML private TableColumn<StudentProfile, String> studentNameColumn;
    @FXML private TableColumn<StudentProfile, String> studentEmailColumn;
    @FXML private TableColumn<StudentProfile, String> studentEnrollmentDateColumn;
    @FXML private TableColumn<StudentProfile, Integer> studentCoursesCountColumn;
    @FXML private TableColumn<StudentProfile, String> studentPasswordRequestColumn;
    @FXML private TableColumn<TeacherProfile, String> teacherIdColumn;
    @FXML private TableColumn<TeacherProfile, String> teacherNameColumn;
    @FXML private TableColumn<TeacherProfile, String> teacherEmailColumn;
    @FXML private TableColumn<TeacherProfile, String> teacherHireDateColumn;
    @FXML private TableColumn<TeacherProfile, Integer> teacherCoursesCountColumn;
    @FXML private TableColumn<TeacherProfile, String> teacherPasswordRequestColumn;
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label totalUsersLabel;

    private final StudentRepository studentRepository = new StudentRepository();
    private final TeacherRepository teacherRepository = new TeacherRepository();
    private final CourseRepository courseRepository = new CourseRepository();
    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
    private final PasswordChangeRepository passwordChangeRepository = new PasswordChangeRepository();
    private final ObservableList<StudentProfile> studentData = FXCollections.observableArrayList();
    private final ObservableList<TeacherProfile> teacherData = FXCollections.observableArrayList();


    @FXML
    private void initialize() {
        setupTables();
        loadStudents();
        loadTeachers();
        studentProfileTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        teacherProfileTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        totalStudentsLabel.setText(String.valueOf(studentRepository.count()));
        totalTeachersLabel.setText(String.valueOf(teacherRepository.count()));
        totalUsersLabel.setText(String.valueOf(studentRepository.count() + teacherRepository.count()));
    }

    @FXML
    private void handleAddStudent() {
        Window owner = addStudentButton.getScene().getWindow();
        SceneManager.showPopup("/hr/javafx/onlinelearningsystem/fxml/admin/AddStudent.fxml", owner);
        loadStudents();
        totalStudentsLabel.setText(String.valueOf(studentRepository.count()));
        totalUsersLabel.setText(String.valueOf(studentRepository.count() + teacherRepository.count()));
    }
    @FXML
    private void handleAddTeacher() {
        Window owner = addTeacherButton.getScene().getWindow();
        SceneManager.showPopup("/hr/javafx/onlinelearningsystem/fxml/admin/AddTeacher.fxml", owner);
        loadTeachers();
        totalTeachersLabel.setText(String.valueOf(teacherRepository.count()));
        totalUsersLabel.setText(String.valueOf(studentRepository.count() + teacherRepository.count()));
    }

    @FXML
    private void handleEditStudent() {
        StudentProfile selectedStudent = studentProfileTable.getSelectionModel().getSelectedItem();
        if(Optional.ofNullable(selectedStudent).isEmpty()) {
            showWarning("Please first select a student you want to edit");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(
                    "/hr/javafx/onlinelearningsystem/fxml/admin/EditStudent.fxml"
            ));
            Parent root = loader.load();
            EditStudentController controller = loader.getController();
            controller.setStudent(selectedStudent);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Online Learning System");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(editStudentButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            loadStudents();
        } catch (IOException e) {
            showError("Failed to open edit screen" + e.getMessage());
        }
    }
    @FXML
    private void handleEditTeacher() {
        TeacherProfile selectedTeacher = teacherProfileTable.getSelectionModel().getSelectedItem();
        if(Optional.ofNullable(selectedTeacher).isEmpty()) {
            showWarning("Please first select a teacher you want to edit");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(
                    "/hr/javafx/onlinelearningsystem/fxml/admin/EditTeacher.fxml"
            ));
            Parent root = loader.load();
            EditTeacherController controller = loader.getController();
            controller.setTeacher(selectedTeacher);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Online Learning System");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(editTeacherButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            loadTeachers();
        } catch (IOException e) {
            showError("Failed to open edit screen" + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveStudent() {
        StudentProfile selectedStudent = studentProfileTable.getSelectionModel().getSelectedItem();
        if(Optional.ofNullable(selectedStudent).isEmpty()) {
            showWarning("Please first select a student you want to remove");
            return;
        }
        Optional<ButtonType> result = showConfirmation("Are you sure you want to remove this student?");

        if(result.isPresent() && result.get() == ButtonType.OK) {
            try {
                enrollmentRepository.unenrollFromAll(selectedStudent.getId());
                UserService.getInstance().removeStudent(selectedStudent.getUsername());
                loadStudents();
                totalStudentsLabel.setText(String.valueOf(studentRepository.count()));
                totalUsersLabel.setText(String.valueOf(studentRepository.count() + teacherRepository.count()));
                showInformation("Student successfully removed.");
            } catch (IOException e) {
                showError("Failed to remove student: " + e.getMessage());
            }
        }
    }
    @FXML
    private void handleRemoveTeacher() {
        TeacherProfile selectedTeacher = teacherProfileTable.getSelectionModel().getSelectedItem();
        if(Optional.ofNullable(selectedTeacher).isEmpty()) {
            showWarning("Please first select a teacher you want to remove");
            return;
        }
        boolean isAssigned = false;
        List<String> coursesTeaching = new ArrayList<>();
        for(Course courses : courseRepository.findAll()) {
            if (courses.getTeacher().getUsername().equals(selectedTeacher.getUsername())) {
                isAssigned = true;
                coursesTeaching.add(courses.getTitle());
            }
        }
        if(isAssigned) {
            showError("This teacher is assigned to courses: " + String.join(", ", coursesTeaching));
            return;
        }
        Optional<ButtonType> result = showConfirmation("Are you sure you want to remove this teacher?");

        if(result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UserService.getInstance().removeTeacher(selectedTeacher.getUsername());
                loadTeachers();
                totalTeachersLabel.setText(String.valueOf(teacherRepository.count()));
                totalUsersLabel.setText(String.valueOf(studentRepository.count() + teacherRepository.count()));
                showInformation("Teacher successfully removed.");
            } catch (IOException e) {
                showError("Failed to remove teacher: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleApproveStudentPasswordRequest() {
        StudentProfile selectedStudent = studentProfileTable.getSelectionModel().getSelectedItem();
        if (Optional.ofNullable(selectedStudent).isEmpty()) {
            showWarning("Please first select a student.");
            return;
        }
        if (!selectedStudent.isRequestedPasswordReset()) {
            showWarning("Selected student has no pending password request.");
            return;
        }

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Approve Password Change");
        passwordDialog.setHeaderText("Set a new password for " + selectedStudent.getUsername());
        passwordDialog.setContentText("New password:");
        stylePasswordDialog(passwordDialog);

        Optional<String> passwordResult = passwordDialog.showAndWait();
        if (passwordResult.isEmpty()) {
            return;
        }

        String newPassword = passwordResult.get().trim();
        if (newPassword.isEmpty()) {
            showWarning("Password cannot be empty.");
            return;
        }

        try {
            UserService.getInstance().updatePassword(selectedStudent.getUsername(), newPassword);
            passwordChangeRepository.markDone(selectedStudent.getUsername(), UserRole.STUDENT);
            showInformation("Password request approved for " + selectedStudent.getUsername() + ".");
            loadStudents();
        } catch (IOException e) {
            showError("Failed to approve student password request: " + e.getMessage());
        }
    }

    @FXML
    private void handleDenyStudentPasswordRequest() {
        StudentProfile selectedStudent = studentProfileTable.getSelectionModel().getSelectedItem();
        if (Optional.ofNullable(selectedStudent).isEmpty()) {
            showWarning("Please first select a student.");
            return;
        }
        if (!selectedStudent.isRequestedPasswordReset()) {
            showWarning("Selected student has no pending password request.");
            return;
        }

        passwordChangeRepository.markDone(selectedStudent.getUsername(), UserRole.STUDENT);
        showInformation("Password request denied for " + selectedStudent.getUsername() + ".");
        loadStudents();
    }

    @FXML
    private void handleApproveTeacherPasswordRequest() {
        TeacherProfile selectedTeacher = teacherProfileTable.getSelectionModel().getSelectedItem();
        if (Optional.ofNullable(selectedTeacher).isEmpty()) {
            showWarning("Please first select a teacher.");
            return;
        }
        if (!selectedTeacher.isRequestedPasswordReset()) {
            showWarning("Selected teacher has no pending password request.");
            return;
        }

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Approve Password Change");
        passwordDialog.setHeaderText("Set a new password for " + selectedTeacher.getUsername());
        passwordDialog.setContentText("New password:");
        stylePasswordDialog(passwordDialog);

        Optional<String> passwordResult = passwordDialog.showAndWait();
        if (passwordResult.isEmpty()) {
            return;
        }

        String newPassword = passwordResult.get().trim();
        if (newPassword.isEmpty()) {
            showWarning("Password cannot be empty.");
            return;
        }

        try {
            UserService.getInstance().updatePassword(selectedTeacher.getUsername(), newPassword);
            passwordChangeRepository.markDone(selectedTeacher.getUsername(), UserRole.TEACHER);
            showInformation("Password request approved for " + selectedTeacher.getUsername() + ".");
            loadTeachers();
        } catch (IOException e) {
            showError("Failed to approve teacher password request: " + e.getMessage());
        }
    }

    @FXML
    private void handleDenyTeacherPasswordRequest() {
        TeacherProfile selectedTeacher = teacherProfileTable.getSelectionModel().getSelectedItem();
        if (Optional.ofNullable(selectedTeacher).isEmpty()) {
            showWarning("Please first select a teacher.");
            return;
        }
        if (!selectedTeacher.isRequestedPasswordReset()) {
            showWarning("Selected teacher has no pending password request.");
            return;
        }

        passwordChangeRepository.markDone(selectedTeacher.getUsername(), UserRole.TEACHER);
        showInformation("Password request denied for " + selectedTeacher.getUsername() + ".");
        loadTeachers();
    }

    private void loadStudents() {
        studentData.clear();
        studentData.addAll(studentRepository.findAll());
    }
    private void loadTeachers() {
        teacherData.clear();
        teacherData.addAll(teacherRepository.findAll());
    }

    private void setupTables() {
        studentJmbagColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getJmbag()));
        studentNameColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        studentEmailColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getEmail()));
        studentEnrollmentDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateAdded()).asString());
        studentCoursesCountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getCoursesCount()));
        studentPasswordRequestColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().isRequestedPasswordReset() ? "YES" : "NO"));
        studentProfileTable.setItems(studentData);

        teacherIdColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getId()).asString());
        teacherNameColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        teacherEmailColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getEmail()));
        teacherHireDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateAdded()).asString());
        teacherCoursesCountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getCoursesCount()));
        teacherPasswordRequestColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().isRequestedPasswordReset() ? "YES" : "NO"));
        teacherProfileTable.setItems(teacherData);
    }

    private void stylePasswordDialog(TextInputDialog dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.setPrefWidth(520);
        pane.setMinWidth(520);
        pane.getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource("/hr/javafx/onlinelearningsystem/styles.css")).toExternalForm());
        pane.getStyleClass().add("card");

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