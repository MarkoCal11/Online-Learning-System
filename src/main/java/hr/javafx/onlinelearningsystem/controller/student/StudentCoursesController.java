package hr.javafx.onlinelearningsystem.controller.student;

import hr.javafx.onlinelearningsystem.enums.EnrolmentStatus;
import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Enrolment;
import hr.javafx.onlinelearningsystem.model.Lesson;
import hr.javafx.onlinelearningsystem.model.Quiz;
import hr.javafx.onlinelearningsystem.model.QuizSubmission;
import hr.javafx.onlinelearningsystem.repository.QuizSubmissionRepository;
import hr.javafx.onlinelearningsystem.repository.EnrollmentRepository;
import hr.javafx.onlinelearningsystem.repository.LessonRepository;
import hr.javafx.onlinelearningsystem.repository.QuizRepository;
import hr.javafx.onlinelearningsystem.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.showError;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showConfirmation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;

public class StudentCoursesController {

    @FXML private VBox coursesContainer;

    private static final String TITLE = "Online learning system";
    private static final String PRIMARY_BUTTON = "primary-button";
    private static final String SECONDARY_BUTTON = "secondary-button";
    private static final DateTimeFormatter QUIZ_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
    private final LessonRepository lessonRepository = new LessonRepository();
    private final QuizRepository quizRepository = new QuizRepository();
    private final QuizSubmissionRepository quizSubmissionRepository = new QuizSubmissionRepository();

    @FXML
    private void initialize() {
        coursesContainer.getChildren().clear();
        String currentUsername = Session.getInstance().getCurrentUser().getUsername();


        List<Enrolment> enrolments = enrollmentRepository.findAll().stream()
                .filter(e -> Optional.ofNullable(e.getStudent())
                        .map(s -> currentUsername.equals(s.getUsername()))
                        .orElse(false))
                .sorted(Comparator
                        .comparing((Enrolment e) -> e.getEnrollmentStatus() == EnrolmentStatus.COMPLETED)
                        .thenComparing(e -> e.getCourse().getTitle(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        for (Enrolment enrolment : enrolments) {
            coursesContainer.getChildren().add(createCourseCard(enrolment));
        }
    }

    private VBox createCourseCard(Enrolment enrolment) {
        Course course = enrolment.getCourse();

        VBox card = new VBox(15.0);
        card.getStyleClass().add("course-card");
        card.setPadding(new Insets(20.0));

        HBox header = new HBox(15.0);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5.0);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label titleLabel = new Label(course.getTitle());
        titleLabel.getStyleClass().add("course-title");

        Label ectsLabel = new Label("ECTS: " + course.getEcts());
        ectsLabel.getStyleClass().add("course-instructor");

        String instructorName = Optional.ofNullable(course.getTeacher())
                .map(t -> t.getFirstName() + " " + t.getLastName())
                .orElse("Not assigned");
        Label instructorLabel = new Label("Instructor: " + instructorName);
        instructorLabel.getStyleClass().add("course-instructor");

        titleBox.getChildren().addAll(titleLabel, ectsLabel, instructorLabel);

        EnrolmentStatus status = enrolment.getEnrollmentStatus();
        Label statusLabel = new Label("Status: " + status.name());
        statusLabel.getStyleClass().add("status-chip");

        if(status.equals(EnrolmentStatus.ACTIVE)) {
            statusLabel.getStyleClass().add("status-active");
        }
        else if (status.equals(EnrolmentStatus.COMPLETED)) {
            statusLabel.getStyleClass().add("status-completed");
        }

        VBox statusBox = new VBox(6.0);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        statusBox.getChildren().add(statusLabel);

        if (status == EnrolmentStatus.COMPLETED) {
            String finalGradeText = enrolment.getFinalGrade()
                    .map(grade -> String.format("Final grade: %.1f", grade))
                    .orElse("Final grade: Not graded");
            Label finalGradeLabel = new Label(finalGradeText);
            finalGradeLabel.getStyleClass().add("final-grade-highlight");
            statusBox.getChildren().add(finalGradeLabel);
        }

        header.getChildren().addAll(titleBox, statusBox);

        Label description = new Label(course.getDescription());
        description.getStyleClass().add("course-description");
        description.setWrapText(true);

        Separator separator1 = new Separator();
        VBox lessonsSection = createLessonsSection(course);

        Separator separator2 = new Separator();
        VBox quizzesSection = createQuizzesSection(enrolment);

        card.getChildren().addAll(header, description, separator1, lessonsSection, separator2, quizzesSection);

        return card;
    }

    private VBox createQuizzesSection(Enrolment enrolment) {
        VBox section = new VBox(10.0);

        HBox sectionHeader = new HBox(10.0);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);

        Label quizzesLabel = new Label("Quizzes");
        quizzesLabel.getStyleClass().add("subsection-label");
        HBox.setHgrow(quizzesLabel, Priority.ALWAYS);

        VBox quizzesList = new VBox(8.0);
        loadQuizzes(quizzesList, enrolment);

        sectionHeader.getChildren().add(quizzesLabel);
        section.getChildren().addAll(sectionHeader, quizzesList);
        return section;
    }

    private void loadQuizzes(VBox quizzesList, Enrolment enrolment) {
        quizzesList.getChildren().clear();
        Course course = enrolment.getCourse();

        for (Quiz quiz : quizRepository.findByCourse(course.getId())) {
            HBox quizItem = new HBox(10.0);
            quizItem.setAlignment(Pos.CENTER_LEFT);
            quizItem.getStyleClass().add("quiz-item");

            Label bullet = new Label("•");
            bullet.getStyleClass().add("bullet-point");

            Label quizLabel = new Label(quiz.getTitle());
            quizLabel.getStyleClass().add("item-text");
            HBox.setHgrow(quizLabel, Priority.ALWAYS);

            Optional<QuizSubmission> existingSubmission = quizSubmissionRepository.findByQuizAndEnrolment(
                    quiz.getId(),
                    enrolment.getId()
            );

            if (existingSubmission.isPresent()) {
                QuizSubmission submission = existingSubmission.get();
                String scoreText = submission.grade() == null || submission.percentage() == null
                        ? "Submitted"
                        : String.format("Score: %d (%.1f%%)", submission.grade(), submission.percentage());
                Label scoreLabel = new Label(scoreText);
                scoreLabel.getStyleClass().add("quiz-score");
                if (submission.percentage() != null && submission.percentage() < 50.0) {
                    scoreLabel.getStyleClass().add("quiz-score-fail");
                }

                Button completedButton = new Button("Completed");
                completedButton.getStyleClass().add(SECONDARY_BUTTON);
                completedButton.setDisable(true);

                quizItem.getChildren().addAll(bullet, quizLabel, scoreLabel, completedButton);
            } else {
                String quizDraft = "quiz-draft";
                if (enrolment.getEnrollmentStatus() == EnrolmentStatus.COMPLETED) {
                    Label completedCourseLabel = new Label("Course completed");
                    completedCourseLabel.getStyleClass().add(quizDraft);
                    quizItem.getChildren().addAll(bullet, quizLabel, completedCourseLabel);
                } else if (quiz.isExpired()) {
                    Label expiredLabel = new Label("Expired");
                    expiredLabel.getStyleClass().add(quizDraft);
                    quizItem.getChildren().addAll(bullet, quizLabel, expiredLabel);
                } else if (!quiz.isReadyToTake()) {
                    Label notReadyLabel = new Label("Not ready to take");
                    notReadyLabel.getStyleClass().add(quizDraft);
                    quizItem.getChildren().addAll(bullet, quizLabel, notReadyLabel);
                } else {
                    String dueDateText = quiz.getDueDate() == null
                            ? "No due date"
                            : "Due: " + quiz.getDueDate().format(QUIZ_DATE_FORMAT);
                    Label dueDateLabel = new Label(dueDateText);
                    dueDateLabel.getStyleClass().add("quiz-pending");

                    Button takeQuizButton = new Button("Take Quiz");
                    takeQuizButton.getStyleClass().add(PRIMARY_BUTTON);
                    takeQuizButton.setOnAction(e -> handleTakeQuiz(quiz, enrolment, quizzesList));

                    quizItem.getChildren().addAll(bullet, quizLabel, dueDateLabel, takeQuizButton);
                }
            }

            quizzesList.getChildren().add(quizItem);
        }
    }

    private void handleTakeQuiz(Quiz quiz, Enrolment enrolment, VBox quizzesList) {
        if (enrolment.getEnrollmentStatus() == EnrolmentStatus.COMPLETED) {
            showWarning("This course is completed, so quizzes can no longer be taken.");
            return;
        }

        if (!quiz.isReadyToTake()) {
            showWarning("This quiz is not ready to take yet.");
            return;
        }

        if (quiz.isExpired()) {
            showWarning("This quiz is expired and can no longer be taken.");
            loadQuizzes(quizzesList, enrolment);
            return;
        }

        if (quizSubmissionRepository.existsByQuizAndEnrolment(quiz.getId(), enrolment.getId())) {
            loadQuizzes(quizzesList, enrolment);
            return;
        }

        Optional<ButtonType> confirmation = showConfirmation(
                "Are you sure you want to start this quiz? Once started, you cannot leave until you submit."
        );
        if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/javafx/onlinelearningsystem/fxml/student/TakeQuiz.fxml"));
            Parent root = loader.load();

            TakeQuizController controller = loader.getController();
            controller.setContext(quiz, enrolment);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(TITLE + " - " + quiz.getTitle());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(coursesContainer.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("/hr/javafx/onlinelearningsystem/styles.css")).toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setOnCloseRequest((WindowEvent event) -> {
                if (!controller.isSubmitted()) {
                    event.consume();
                    showWarning("You cannot close this quiz before submitting.");
                }
            });
            dialogStage.setResizable(true);
            dialogStage.showAndWait();

            loadQuizzes(quizzesList, enrolment);
        } catch (IOException e) {
            showError("Failed to open quiz: " + e.getMessage());
        }
    }

    private VBox createLessonsSection(Course course) {

        VBox section = new VBox(10.0);

        HBox sectionHeader = new HBox(10.0);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);

        Label lessonsLabel = new Label("Lessons");
        lessonsLabel.getStyleClass().add("subsection-label");
        HBox.setHgrow(lessonsLabel, Priority.ALWAYS);

        VBox lessonsList = new VBox(8.0);
        loadLessons(lessonsList, course);

        sectionHeader.getChildren().addAll(lessonsLabel);

        section.getChildren().addAll(sectionHeader, lessonsList);
        return section;
    }

    private void loadLessons(VBox lessonsList, Course course) {
        lessonsList.getChildren().clear();
        for (Lesson lesson : lessonRepository.findByCourse(course.getId())) {
            HBox lessonItem = new HBox(10.0);
            lessonItem.setAlignment(Pos.CENTER_LEFT);
            lessonItem.getStyleClass().add("lesson-item");

            Label bullet = new Label("•");
            bullet.getStyleClass().add("bullet-point");

            Label lessonLabel = new Label(lesson.getTitle());
            lessonLabel.getStyleClass().add("item-text");
            HBox.setHgrow(lessonLabel, Priority.ALWAYS);

            Button openButton = new Button("Open");
            openButton.getStyleClass().add(SECONDARY_BUTTON);
            openButton.setOnAction(e -> handleViewLesson(lesson, course));


            lessonItem.getChildren().addAll(bullet, lessonLabel, openButton);
            lessonsList.getChildren().add(lessonItem);
        }
    }

    private void handleViewLesson(Lesson lesson, Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/javafx/onlinelearningsystem/fxml/student/ViewLesson.fxml"));
            Parent root = loader.load();
            ViewLessonController controller = loader.getController();
            controller.setLesson(lesson, course);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Lesson");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(coursesContainer.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass()
                            .getResource("/hr/javafx/onlinelearningsystem/styles.css")).toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showError("Failed to open lesson: " + e.getMessage());
        }
    }
}
