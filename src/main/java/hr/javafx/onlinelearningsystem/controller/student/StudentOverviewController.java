package hr.javafx.onlinelearningsystem.controller.student;


import hr.javafx.onlinelearningsystem.enums.EnrolmentStatus;
import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Enrolment;
import hr.javafx.onlinelearningsystem.model.Quiz;
import hr.javafx.onlinelearningsystem.model.QuizSubmission;
import hr.javafx.onlinelearningsystem.model.TeacherProfile;
import hr.javafx.onlinelearningsystem.repository.EnrollmentRepository;
import hr.javafx.onlinelearningsystem.repository.QuizRepository;
import hr.javafx.onlinelearningsystem.repository.QuizSubmissionRepository;
import hr.javafx.onlinelearningsystem.util.Session;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StudentOverviewController {

    @FXML private Label totalCoursesLabel;
    @FXML private Label totalActiveEctsLabel;
    @FXML private Label totalAcquiredEctsLabel;

    @FXML private TableView<Course> activeCoursesTable;
    @FXML private TableColumn<Course, String> titleColumn;
    @FXML private TableColumn<Course, String> teacherColumn;
    @FXML private TableColumn<Course, Number> ectsColumn;

    @FXML private TableView<Quiz> pendingTestsTable;
    @FXML private TableColumn<Quiz, String> pendingTitleColumn;
    @FXML private TableColumn<Quiz, String> pendingClassColumn;
    @FXML private TableColumn<Quiz, LocalDate> pendingDueDateColumn;

    @FXML private TableView<QuizSubmission> completedTestsTable;
    @FXML private TableColumn<QuizSubmission, String> completedTitleColumn;
    @FXML private TableColumn<QuizSubmission, String> completedClassColumn;
    @FXML private TableColumn<QuizSubmission, String> completedSubmitDateColumn;
    @FXML private TableColumn<QuizSubmission, String> completedGradeColumn;

    private static final DateTimeFormatter SUBMISSION_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
    private final QuizRepository quizRepository = new QuizRepository();
    private final QuizSubmissionRepository submissionRepository = new QuizSubmissionRepository();

    private final ObservableList<Course> activeCoursesData = FXCollections.observableArrayList();
    private final ObservableList<Quiz> pendingTestsData = FXCollections.observableArrayList();
    private final ObservableList<QuizSubmission> completedTestsData = FXCollections.observableArrayList();
    private final Map<Long, String> quizTitlesById = new HashMap<>();
    private final Map<Long, String> quizCourseTitlesById = new HashMap<>();

    @FXML
    private void initialize() {
        Long currentId = Session.getInstance().getCurrentUser().getId();

        setupActiveCoursesTable();
        setupPendingTestsTable();
        setupCompletedTestsTable();

        List<Enrolment> studentEnrolments = loadStudentEnrolments(currentId);
        updateData(studentEnrolments);
        activeCoursesData.setAll(loadActiveCourses(studentEnrolments));
        pendingTestsData.setAll(loadPendingTests(studentEnrolments));
        completedTestsData.setAll(loadCompletedTests(studentEnrolments));

        totalCoursesLabel.setText(String.valueOf(activeCoursesData.size()));
        totalActiveEctsLabel.setText(String.valueOf(countActiveEcts(activeCoursesData)));
        totalAcquiredEctsLabel.setText(String.valueOf(countAcquiredEcts(studentEnrolments)));
    }

    private void setupActiveCoursesTable() {
        titleColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTitle()));
        teacherColumn.setCellValueFactory(cellData -> {
            TeacherProfile teacher = cellData.getValue().getTeacher();
            return new SimpleObjectProperty<>(teacher == null ? "Vacant" : 
                    teacher.getFirstName() + " " + teacher.getLastName());
        });
        ectsColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEcts()));
        activeCoursesTable.setItems(activeCoursesData);
    }

    private void setupPendingTestsTable() {
        pendingTitleColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTitle()));
        pendingClassColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                getCourseTitle(cellData.getValue().getId())
        ));
        pendingDueDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDueDate()));
        pendingTestsTable.setItems(pendingTestsData);
    }

    private void setupCompletedTestsTable() {
        completedTitleColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                getQuizTitle(cellData.getValue())
        ));
        completedClassColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                getCourseTitle(cellData.getValue().quiz().getId())
        ));
        completedSubmitDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(formatSubmissionDate(cellData.getValue().submittedAt())));
        completedGradeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(formatGrade(cellData.getValue())));
        completedTestsTable.setItems(completedTestsData);
    }

    private void updateData(List<Enrolment> studentEnrolments) {
        quizTitlesById.clear();
        quizCourseTitlesById.clear();

        for (Enrolment enrolment : studentEnrolments) {
            for (Quiz quiz : quizRepository.findByCourse(enrolment.getCourse().getId())) {
                quizTitlesById.put(quiz.getId(), quiz.getTitle());
                quizCourseTitlesById.put(quiz.getId(), enrolment.getCourse().getTitle());
            }
        }
    }

    private List<Enrolment> loadStudentEnrolments(Long currentId) {
        return enrollmentRepository.findAll().stream()
                .filter(e -> e.getStudent() != null)
                .filter(e -> currentId.equals(e.getStudent().getId()))
                .toList();
    }

    private List<Course> loadActiveCourses(List<Enrolment> studentEnrolments) {

        return studentEnrolments.stream()
                .filter(e -> e.getEnrollmentStatus() == EnrolmentStatus.ACTIVE)
                .map(Enrolment::getCourse)
                .toList();
    }

    private List<Quiz> loadPendingTests(List<Enrolment> studentEnrolments) {
        return studentEnrolments.stream()
                .filter(e -> e.getEnrollmentStatus() == EnrolmentStatus.ACTIVE)
                .flatMap(enrolment -> quizRepository.findByCourse(enrolment.getCourse().getId()).stream()
                        .filter(Quiz::canBeTaken)
                        .filter(quiz -> !submissionRepository.existsByQuizAndEnrolment(quiz.getId(), enrolment.getId())))
                .sorted(Comparator.comparing(Quiz::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<QuizSubmission> loadCompletedTests(List<Enrolment> studentEnrolments) {
        return studentEnrolments.stream()
                .flatMap(enrolment -> quizRepository.findByCourse(enrolment.getCourse().getId()).stream()
                        .map(quiz -> submissionRepository.findByQuizAndEnrolment(quiz.getId(), enrolment.getId()).orElse(null)))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(QuizSubmission::submittedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private String getQuizTitle(QuizSubmission submission) {
        Long quizId = submission.quiz().getId();
        return quizTitlesById.getOrDefault(quizId, "Quiz #" + quizId);
    }

    private String getCourseTitle(Long quizId) {
        return quizCourseTitlesById.getOrDefault(quizId, "-");
    }

    private String formatGrade(QuizSubmission submission) {
        if (submission.grade() == null || submission.percentage() == null) {
            return "Submitted";
        }
        return String.format("%d (%.1f%%)", submission.grade(), submission.percentage());
    }

    private String formatSubmissionDate(LocalDateTime submittedAt) {
        if (submittedAt == null) {
            return "-";
        }
        return submittedAt.format(SUBMISSION_DATE_FORMATTER);
    }

    private Integer countActiveEcts(List<Course> activeCourses) {
        int totalEcts = 0;

        for (Course course : activeCourses) {
            totalEcts += Optional.ofNullable(course.getEcts()).orElse(0);
        }
        return totalEcts;
    }

    private Integer countAcquiredEcts(List<Enrolment> studentEnrolments) {
        int totalEcts = 0;

        for (Enrolment enrolment : studentEnrolments) {
            if (enrolment.getEnrollmentStatus() == EnrolmentStatus.COMPLETED && enrolment.getCourse() != null) {
                totalEcts += Optional.ofNullable(enrolment.getCourse().getEcts()).orElse(0);
            }
        }

        return totalEcts;
    }

}