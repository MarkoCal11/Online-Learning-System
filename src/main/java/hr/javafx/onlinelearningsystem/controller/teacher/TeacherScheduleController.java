package hr.javafx.onlinelearningsystem.controller.teacher;

import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Quiz;
import hr.javafx.onlinelearningsystem.repository.CourseRepository;
import hr.javafx.onlinelearningsystem.repository.EnrollmentRepository;
import hr.javafx.onlinelearningsystem.repository.QuizRepository;
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

public class TeacherScheduleController {

	@FXML private Label weekRangeLabel;
	@FXML private VBox scheduleContent;

	private final CourseRepository courseRepository = new CourseRepository();
	private final QuizRepository quizRepository = new QuizRepository();
	private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
	private final Map<Long, String> courseTitlesByQuizId = new HashMap<>();
	private final Map<Long, Integer> studentCountsByQuizId = new HashMap<>();
	private final Map<Long, String> quizStatusById = new HashMap<>();

	private static final DateTimeFormatter WEEK_RANGE_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d");
	private static final DateTimeFormatter DAY_HEADER_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d");

	private static final String UPCOMING = "Upcoming";

	private LocalDate currentWeekStart;

	@FXML
	private void initialize() {
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

		List<Quiz> entries = loadScheduledQuizzesForCurrentTeacher();

		Map<LocalDate, List<Quiz>> byDate = entries.stream()
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

	private List<Quiz> loadScheduledQuizzesForCurrentTeacher() {
		String currentUsername = Session.getInstance().getCurrentUser().getUsername();
		courseTitlesByQuizId.clear();
		studentCountsByQuizId.clear();
		quizStatusById.clear();

		List<Course> teacherCourses = courseRepository.findAll().stream()
				.filter(course -> course.getTeacher() != null)
				.filter(course -> currentUsername.equals(course.getTeacher().getUsername()))
				.toList();

		List<Quiz> entries = new java.util.ArrayList<>();
		for (Course course : teacherCourses) {
			Integer studentCount = enrollmentRepository.countStudentsByCourse(course);
			for (Quiz quiz : quizRepository.findByCourse(course.getId())) {
				if (quiz.getDueDate() != null) {
					entries.add(quiz);
					courseTitlesByQuizId.put(quiz.getId(), course.getTitle());
					studentCountsByQuizId.put(quiz.getId(), studentCount);
					quizStatusById.put(quiz.getId(), resolveTeacherQuizStatus(quiz));
				}
			}
		}

		return entries;
	}

	private VBox createDayCard(LocalDate day, List<Quiz> dayEntries) {
		VBox card = new VBox(10.0);
		card.getStyleClass().add("card");
		card.setPadding(new Insets(15.0));

		Label dayLabel = new Label(day.format(DAY_HEADER_FORMAT));
		dayLabel.getStyleClass().add("stat-label");
		card.getChildren().add(dayLabel);

		for (Quiz entry : dayEntries) {
			HBox row = new HBox(15.0);
			row.setAlignment(Pos.CENTER_LEFT);
			row.getStyleClass().add("schedule-item");

			VBox left = new VBox();
			left.setMinWidth(80.0);
			left.setAlignment(Pos.CENTER_LEFT);
			Label dueLabel = new Label(quizStatusById.getOrDefault(entry.getId(), UPCOMING));
			dueLabel.getStyleClass().add("time-label");
			left.getChildren().add(dueLabel);

			VBox center = new VBox(5.0);
			HBox.setHgrow(center, Priority.ALWAYS);
			Label title = new Label(entry.getTitle());
			title.getStyleClass().add("event-title");
			Label detail = new Label(formatQuizDetail(entry));
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

		Label title = new Label("No scheduled quizzes for this week");
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

	private String formatQuizDetail(Quiz quiz) {
		String courseTitle = courseTitlesByQuizId.getOrDefault(quiz.getId(), "-");
		Integer studentCount = studentCountsByQuizId.getOrDefault(quiz.getId(), 0);
		String state = quizStatusById.getOrDefault(quiz.getId(), UPCOMING);
		return "Course: " + courseTitle + " • " + studentCount + " students • " + state;
	}

	private String resolveTeacherQuizStatus(Quiz quiz) {
		if (quiz.getDueDate() != null && quiz.getDueDate().isBefore(LocalDate.now())) {
			return "Finished";
		}
		return UPCOMING;
	}
}
