package hr.javafx.onlinelearningsystem.controller.admin;

import hr.javafx.onlinelearningsystem.model.UserProfile;
import hr.javafx.onlinelearningsystem.repository.*;
import hr.javafx.onlinelearningsystem.model.Enrolment;
import hr.javafx.onlinelearningsystem.service.AuditLogEvent;
import hr.javafx.onlinelearningsystem.service.AuditLogService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class AdminOverviewController {

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalQuizzesLabel;
    @FXML private Label totalLessonsLabel;
    @FXML private Label totalEnrollmentsLabel;

    @FXML private TableView<UserProfile> userActivityTable;
    @FXML private TableColumn<UserProfile, String> userNameColumn;
    @FXML private TableColumn<UserProfile, String> roleColumn;
    @FXML private TableColumn<UserProfile, String> activityColumn;
    @FXML private TableColumn<UserProfile, LocalDate> activityDateColumn;

    @FXML private TableView<Enrolment> enrolmentsTable;
    @FXML private TableColumn<Enrolment, String> studentColumn;
    @FXML private TableColumn<Enrolment, String> courseColumn;
    @FXML private TableColumn<Enrolment, String> teacherColumn;
    @FXML private TableColumn<Enrolment, LocalDate> dateColumn;

    @FXML private TableView<AuditLogEvent> auditEventsTable;
    @FXML private TableColumn<AuditLogEvent, String> auditTimestampColumn;
    @FXML private TableColumn<AuditLogEvent, String> auditUsernameColumn;
    @FXML private TableColumn<AuditLogEvent, String> auditReasonColumn;


    private final StudentRepository studentRepository = new StudentRepository();
    private final TeacherRepository teacherRepository = new TeacherRepository();
    private final CourseRepository courseRepository = new CourseRepository();
    private final QuizRepository quizRepository = new QuizRepository();
    private final LessonRepository lessonRepository = new LessonRepository();
    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
    private final AuditLogService auditLogService = new AuditLogService();

    private final ObservableList<Enrolment> enrolmentData = FXCollections.observableArrayList();
    private final ObservableList<UserProfile> userActivityData = FXCollections.observableArrayList();
    private final ObservableList<AuditLogEvent> auditEventData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTables();
        loadUserActivity();
        loadEnrollments();
        loadAuditEvents();
        totalStudentsLabel.setText(String.valueOf(studentRepository.count()));
        totalTeachersLabel.setText(String.valueOf(teacherRepository.count()));
        totalCoursesLabel.setText(String.valueOf(courseRepository.countCourses()));
        totalQuizzesLabel.setText(String.valueOf(quizRepository.findAll().size()));
        totalLessonsLabel.setText(String.valueOf(lessonRepository.count()));
        totalEnrollmentsLabel.setText(String.valueOf(courseRepository.countEnrollments()));
    }

    private void loadUserActivity() {
        List<UserProfile> rows = Stream.concat(
                        studentRepository.findAll().stream().map(UserProfile.class::cast),
                        teacherRepository.findAll().stream().map(UserProfile.class::cast)
                )
                .sorted(Comparator.comparing(UserProfile::getDateAdded, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(20)
                .toList();

        userActivityData.setAll(rows);
    }

    private void loadEnrollments() {
        enrolmentData.clear();
        enrolmentData.addAll(enrollmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Enrolment::getEnrollmentDate, Comparator.reverseOrder()))
                .limit(20)
                .toList());
    }

    private void loadAuditEvents() {
        auditEventData.setAll(auditLogService.getRecentAuthenticationFailures(20));
    }

    private void setupTables() {
        userNameColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        roleColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getRole().name()));
        activityColumn.setCellValueFactory(ignoredCellData ->
                new SimpleObjectProperty<>("Account created"));
        activityDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateAdded()));
        userActivityTable.setItems(userActivityData);

        studentColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getStudent().getFirstName()
                        + " " + cellData.getValue().getStudent().getLastName()));
        courseColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getCourse().getTitle()));
        teacherColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getCourse().getTeacher() == null
                        ? "Vacant"
                        : cellData.getValue().getCourse().getTeacher().getFirstName()
                        + " " + cellData.getValue().getCourse().getTeacher().getLastName()));
        dateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getEnrollmentDate()));

        enrolmentsTable.setItems(enrolmentData);

        auditTimestampColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().timestamp()));
        auditUsernameColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().username()));
        auditReasonColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().reason()));
        auditEventsTable.setItems(auditEventData);
    }

}