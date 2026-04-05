package hr.javafx.onlinelearningsystem.controller.teacher;

import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Enrolment;
import hr.javafx.onlinelearningsystem.model.Quiz;
import hr.javafx.onlinelearningsystem.model.QuizSubmission;
import hr.javafx.onlinelearningsystem.model.TeacherProfile;
import hr.javafx.onlinelearningsystem.repository.CourseRepository;
import hr.javafx.onlinelearningsystem.repository.EnrollmentRepository;
import hr.javafx.onlinelearningsystem.repository.QuizRepository;
import hr.javafx.onlinelearningsystem.repository.QuizSubmissionRepository;
import hr.javafx.onlinelearningsystem.repository.TeacherRepository;
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
import java.util.Set;

public class TeacherOverviewController {

    @FXML private Label totalCoursesLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label upcomingQuizzesLabel;

    @FXML private TableView<Quiz> upcomingQuizzesTable;
    @FXML private TableColumn<Quiz, String> upcomingQuizTitleColumn;
    @FXML private TableColumn<Quiz, String> upcomingQuizCourseColumn;
    @FXML private TableColumn<Quiz, LocalDate> upcomingQuizDueDateColumn;
    @FXML private TableColumn<Quiz, String> upcomingQuizActionColumn;

    @FXML private TableView<QuizSubmission> recentSubmissionsTable;
    @FXML private TableColumn<QuizSubmission, String> recentSubmissionStudentColumn;
    @FXML private TableColumn<QuizSubmission, String> recentSubmissionQuizColumn;
    @FXML private TableColumn<QuizSubmission, String> recentSubmissionCourseColumn;
    @FXML private TableColumn<QuizSubmission, String> recentSubmissionGradeColumn;
    @FXML private TableColumn<QuizSubmission, String> recentSubmissionDateColumn;

    private static final DateTimeFormatter SUBMISSION_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final CourseRepository courseRepository = new CourseRepository();
    private final TeacherRepository teacherRepository = new TeacherRepository();
    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
    private final QuizRepository quizRepository = new QuizRepository();
    private final QuizSubmissionRepository submissionRepository = new QuizSubmissionRepository();

    private final ObservableList<Quiz> upcomingQuizData = FXCollections.observableArrayList();
    private final ObservableList<QuizSubmission> recentSubmissionData = FXCollections.observableArrayList();
    private final Map<Long, String> teacherCourseTitlesById = new HashMap<>();
    private final Map<Long, Quiz> quizzesById = new HashMap<>();
    private final Map<Long, Enrolment> enrolmentsById = new HashMap<>();

    @FXML
    private void initialize() {
        setupTables();

        String currentUsername = Session.getInstance().getCurrentUser().getUsername();
        TeacherProfile teacherProfile = teacherRepository.findByUsername(currentUsername);

        List<Course> teacherCourses = courseRepository.findAll().stream()
                .filter(course -> course.getTeacher() != null)
                .filter(course -> teacherProfile.getId().equals(course.getTeacher().getId()))
                .toList();

        totalCoursesLabel.setText(String.valueOf(teacherCourses.size()));

        Integer totalStudents = 0;
        for (Course course : teacherCourses) {
            if (course.getTeacher() != null) {
                totalStudents += enrollmentRepository.countStudentsByCourse(course);
            }
        }
        totalStudentsLabel.setText(String.valueOf(totalStudents));

        populateUpcomingQuizzes(teacherCourses);
        upcomingQuizzesLabel.setText(String.valueOf(upcomingQuizData.size()));
        populateRecentSubmissions();
    }

    private void setupTables() {
        upcomingQuizTitleColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTitle()));
        upcomingQuizCourseColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                teacherCourseTitlesById.getOrDefault(cellData.getValue().getCourse().getId(), "-")
        ));
        upcomingQuizDueDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDueDate()));
        upcomingQuizActionColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().isReadyToTake() ? "Ready" : "Draft"));
        upcomingQuizzesTable.setItems(upcomingQuizData);

        recentSubmissionStudentColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(resolveStudentName(cellData.getValue())));
        recentSubmissionQuizColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(resolveQuizTitle(cellData.getValue())));
        recentSubmissionCourseColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(resolveCourseTitle(cellData.getValue())));
        recentSubmissionGradeColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(formatGrade(cellData.getValue())));
        recentSubmissionDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(formatSubmissionDate(cellData.getValue().submittedAt())));
        recentSubmissionsTable.setItems(recentSubmissionData);
    }

    private void populateUpcomingQuizzes(List<Course> teacherCourses) {
        teacherCourseTitlesById.clear();
        for (Course course : teacherCourses) {
            teacherCourseTitlesById.put(course.getId(), course.getTitle());
        }

        quizzesById.clear();
        List<Quiz> allQuizzes = quizRepository.findAll();
        for (Quiz quiz : allQuizzes) {
            quizzesById.put(quiz.getId(), quiz);
        }

        Set<Long> teacherCourseIds = teacherCourseTitlesById.keySet();
        List<Quiz> rows = allQuizzes.stream()
                .filter(quiz -> quiz.getCourse() != null)
                .filter(quiz -> teacherCourseIds.contains(quiz.getCourse().getId()))
                .filter(quiz -> !quiz.isExpired())
                .sorted(Comparator.comparing(Quiz::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        upcomingQuizData.setAll(rows);
    }

    private void populateRecentSubmissions() {
        Set<Long> teacherCourseIds = teacherCourseTitlesById.keySet();

        enrolmentsById.clear();
        for (Enrolment enrolment : enrollmentRepository.findAll()) {
            enrolmentsById.put(enrolment.getId(), enrolment);
        }

        List<QuizSubmission> rows = submissionRepository.findAll().stream()
                .filter(submission -> submission.quiz() != null)
                .filter(submission -> isSubmissionForTeacherCourse(submission, teacherCourseIds))
                .sorted(Comparator.comparing(QuizSubmission::submittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        recentSubmissionData.setAll(rows);
    }

    private boolean isSubmissionForTeacherCourse(QuizSubmission submission, Set<Long> teacherCourseIds) {
        Quiz quiz = quizzesById.get(submission.quiz().getId());
        return quiz != null
                && quiz.getCourse() != null
                && teacherCourseIds.contains(quiz.getCourse().getId());
    }

    private String resolveStudentName(QuizSubmission submission) {
        Enrolment enrolment = enrolmentsById.get(submission.enrolledStudent().getId());
        if (enrolment == null || enrolment.getStudent() == null) {
            return "Unknown";
        }
        return enrolment.getStudent().getFirstName() + " " + enrolment.getStudent().getLastName();
    }

    private String resolveQuizTitle(QuizSubmission submission) {
        Quiz quiz = quizzesById.get(submission.quiz().getId());
        return quiz == null ? "Quiz #" + submission.quiz().getId() : quiz.getTitle();
    }

    private String resolveCourseTitle(QuizSubmission submission) {
        Quiz quiz = quizzesById.get(submission.quiz().getId());
        if (quiz == null || quiz.getCourse() == null) {
            return "-";
        }
        return teacherCourseTitlesById.getOrDefault(quiz.getCourse().getId(), "-");
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
}
