package hr.javafx.onlinelearningsystem.controller.student;

import hr.javafx.onlinelearningsystem.enums.EnrolmentStatus;
import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Enrolment;
import hr.javafx.onlinelearningsystem.model.Quiz;
import hr.javafx.onlinelearningsystem.repository.EnrollmentRepository;
import hr.javafx.onlinelearningsystem.repository.QuizRepository;
import hr.javafx.onlinelearningsystem.repository.QuizSubmissionRepository;
import hr.javafx.onlinelearningsystem.util.Session;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentScheduleController {

    @FXML private Label weekRangeLabel;
    @FXML private VBox scheduleContent;

    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
    private final QuizRepository quizRepository = new QuizRepository();
    private final QuizSubmissionRepository submissionRepository = new QuizSubmissionRepository();
    private final Map<Long, String> quizCourseTitlesById = new HashMap<>();
    private final Map<Long, String> quizStatusById = new HashMap<>();

    private static final DateTimeFormatter WEEK_RANGE_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter DAY_HEADER_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d");

    private LocalDate currentWeekStart;

    @FXML
    public void initialize() {
        handleToday();
    }

    @FXML
    private void handlePreviousWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        renderWeek();
    }

    @FXML
    private void handleToday() {
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        renderWeek();
    }

    @FXML
    private void handleNextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        renderWeek();
    }

    private void renderWeek() {
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        weekRangeLabel.setText(formatWeekRange(currentWeekStart, weekEnd));
        scheduleContent.getChildren().clear();

        List<Quiz> quizzes = loadScheduledQuizzesForCurrentStudent();

        Map<LocalDate, List<Quiz>> byDate = quizzes.stream()
                .filter(quiz -> quiz.getDueDate() != null)
                .filter(quiz -> !quiz.getDueDate().isBefore(currentWeekStart) && !quiz.getDueDate().isAfter(weekEnd))
                .collect(Collectors.groupingBy(Quiz::getDueDate));

        for (LocalDate day = currentWeekStart; !day.isAfter(weekEnd); day = day.plusDays(1)) {
            List<Quiz> dayEntries = byDate.getOrDefault(day, List.of()).stream()
                    .sorted(Comparator.comparing(Quiz::getTitle, String.CASE_INSENSITIVE_ORDER))
                    .toList();

            if (!dayEntries.isEmpty()) {
                scheduleContent.getChildren().add(createDayCard(day, dayEntries));
            }
        }

        if (scheduleContent.getChildren().isEmpty()) {
            scheduleContent.getChildren().add(createEmptyStateCard());
        }
    }

    private List<Quiz> loadScheduledQuizzesForCurrentStudent() {
        Long currentUserId = Session.getInstance().getCurrentUser().getId();
        quizCourseTitlesById.clear();
        quizStatusById.clear();

        List<Enrolment> activeEnrolments = enrollmentRepository.findAll().stream()
                .filter(enrolment -> enrolment.getStudent() != null)
                .filter(enrolment -> currentUserId.equals(enrolment.getStudent().getId()))
                .filter(enrolment -> enrolment.getEnrollmentStatus() == EnrolmentStatus.ACTIVE)
                .toList();

        List<Quiz> scheduledQuizzes = new java.util.ArrayList<>();
        for (Enrolment enrolment : activeEnrolments) {
            Course course = enrolment.getCourse();
            for (Quiz quiz : quizRepository.findByCourse(course.getId())) {
                if (quiz.getDueDate() != null) {
                    scheduledQuizzes.add(quiz);
                    quizCourseTitlesById.put(quiz.getId(), course.getTitle());
                    quizStatusById.put(quiz.getId(), resolveStudentQuizStatus(quiz, enrolment));
                }
            }
        }

        return scheduledQuizzes;
    }

    private VBox createDayCard(LocalDate day, List<Quiz> dayEntries) {
        VBox card = new VBox(10.0);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15.0));

        Label dayLabel = new Label(day.format(DAY_HEADER_FORMAT));
        dayLabel.getStyleClass().add("stat-label");
        card.getChildren().add(dayLabel);

        for (Quiz quiz : dayEntries) {
            HBox row = new HBox(15.0);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("schedule-item");

            VBox left = new VBox();
            left.setMinWidth(80.0);
            left.setAlignment(Pos.CENTER_LEFT);
            Label dueLabel = new Label(quizStatusById.getOrDefault(quiz.getId(), "Upcoming"));
            dueLabel.getStyleClass().add("time-label");
            left.getChildren().add(dueLabel);

            VBox center = new VBox(5.0);
            HBox.setHgrow(center, Priority.ALWAYS);
            Label title = new Label(quiz.getTitle());
            title.getStyleClass().add("event-title");
            String quizState = quiz.isReadyToTake() ? "Ready" : "Not ready";
            Label detail = new Label(
                    "Course: " + quizCourseTitlesById.getOrDefault(quiz.getId(), "-") + " • " + quizState
            );
            detail.getStyleClass().add("event-detail");
            center.getChildren().addAll(title, detail);

            row.getChildren().addAll(left, center);
            card.getChildren().add(row);
        }

        return card;
    }

    private VBox createEmptyStateCard() {
        VBox card = new VBox(10.0);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15.0));

        Label title = new Label("No scheduled tests for this week");
        title.getStyleClass().add("event-title");
        Label detail = new Label("Try another week using the navigation buttons above.");
        detail.getStyleClass().add("event-detail");
        card.getChildren().addAll(title, detail);
        return card;
    }

    private String formatWeekRange(LocalDate weekStart, LocalDate weekEnd) {
        return weekStart.format(WEEK_RANGE_DATE_FORMAT)
                + " - "
                + weekEnd.format(WEEK_RANGE_DATE_FORMAT)
                + ", "
                + weekEnd.getYear();
    }

    private String resolveStudentQuizStatus(Quiz quiz, Enrolment enrolment) {
        if (submissionRepository.existsByQuizAndEnrolment(quiz.getId(), enrolment.getId())) {
            return "Taken";
        }
        if (quiz.isReadyToTake()) {
            return "Due";
        }
        return "Upcoming";
    }


}
