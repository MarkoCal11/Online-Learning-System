package hr.javafx.onlinelearningsystem.controller.teacher;

import hr.javafx.onlinelearningsystem.model.*;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.repository.CourseRepository;
import hr.javafx.onlinelearningsystem.repository.EnrollmentRepository;
import hr.javafx.onlinelearningsystem.repository.LessonRepository;
import hr.javafx.onlinelearningsystem.repository.QuestionRepository;
import hr.javafx.onlinelearningsystem.repository.QuizRepository;
import hr.javafx.onlinelearningsystem.repository.QuizSubmissionRepository;
import hr.javafx.onlinelearningsystem.util.SceneManager;
import hr.javafx.onlinelearningsystem.util.Session;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.*;

public class TeacherCoursesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherCoursesController.class);

    @FXML private VBox coursesContainer;

    private static final String PRIMARY_BUTTON = "primary-button";
    private static final String SECONDARY_BUTTON = "secondary-button";
    private static final String TITLE = "Online learning system";
    private static final String UNKNOWN = "Unknown";
    private static final String STAT_LABEL = "stat-label";

    private final CourseRepository courseRepository = new CourseRepository();
    private final LessonRepository lessonRepository = new LessonRepository();
    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
    private final QuizRepository quizRepository = new QuizRepository();
    private final QuestionRepository questionRepository = new QuestionRepository();
    private final QuizSubmissionRepository submissionRepository = new QuizSubmissionRepository();
    private static final DateTimeFormatter QUIZ_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter SUBMISSION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    private void initialize() {
        reloadCourseCards();
    }

    private void reloadCourseCards() {
        coursesContainer.getChildren().clear();

        String currentUsername = Session.getInstance().getCurrentUser().getUsername();
        List<Course> courses = new ArrayList<>();

        for (Course course : courseRepository.findAll()) {
            if (Optional.ofNullable(course.getTeacher()).isPresent() && course.getTeacher().getUsername().equals(currentUsername)) {
                courses.add(course);
            }
        }

        for (Course course : courses) {
            VBox courseCard = createCourseCard(course);
            coursesContainer.getChildren().add(courseCard);
        }
    }

    private VBox createCourseCard(Course course) {

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

        Label enrollmentLabel = new Label("Students Enrolled: " + enrollmentRepository.countStudentsByCourse(course));
        enrollmentLabel.getStyleClass().add("course-instructor");

        titleBox.getChildren().addAll(titleLabel, ectsLabel, enrollmentLabel);

        Button viewStudentsButton = new Button("View Students");
        viewStudentsButton.getStyleClass().add(SECONDARY_BUTTON);
        viewStudentsButton.setOnAction(actionEvent -> handleShowEnrollments(course));

        Label averageGradeLabel = new Label(getAverageFinalGradeText(course));
        averageGradeLabel.getStyleClass().add("course-average-badge");

        VBox rightInfoBox = new VBox(8.0);
        rightInfoBox.setAlignment(Pos.CENTER_RIGHT);
        rightInfoBox.getChildren().addAll(averageGradeLabel, viewStudentsButton);

        header.getChildren().addAll(titleBox, rightInfoBox);

        Label description = new Label(course.getDescription());
        description.getStyleClass().add("course-description");
        description.setWrapText(true);

        Separator separator1 = new Separator();
        VBox lessonsSection = createLessonsSection(course);

        Separator separator2 = new Separator();
        VBox quizzesSection = createQuizzesSection(course);

        card.getChildren().addAll(header, description, separator1, lessonsSection, separator2, quizzesSection);

        return card;
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

        Button addLessonButton = new Button("+ Add Lesson");
        addLessonButton.getStyleClass().add(PRIMARY_BUTTON);
        addLessonButton.setOnAction(actionEvent -> handleAddLesson(course, lessonsList));

        sectionHeader.getChildren().addAll(lessonsLabel, addLessonButton);

        section.getChildren().addAll(sectionHeader, lessonsList);
        return section;
    }

    private String getAverageFinalGradeText(Course course) {
        OptionalDouble average = enrollmentRepository.findAll().stream()
                .filter(enrolment -> enrolment.getCourse() != null)
                .filter(enrolment -> course.getId().equals(enrolment.getCourse().getId()))
                .map(Enrolment::getFinalGrade)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .mapToDouble(Double::doubleValue)
                .average();

        if (average.isEmpty()) {
            return "Average: -";
        }

        return String.format("Average: %.2f", average.getAsDouble());
    }

    private VBox createQuizzesSection(Course course) {
        VBox section = new VBox(10.0);

        HBox sectionHeader = new HBox(10.0);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);

        Label quizzesLabel = new Label("Quizzes");
        quizzesLabel.getStyleClass().add("subsection-label");
        HBox.setHgrow(quizzesLabel, Priority.ALWAYS);

        Button addQuizButton = new Button("+ Add Quiz");
        addQuizButton.getStyleClass().add(PRIMARY_BUTTON);

        VBox quizzesList = new VBox(8.0);
        loadQuizzes(quizzesList, course);
        addQuizButton.setOnAction(actionEvent -> handleAddQuiz(course, quizzesList));

        sectionHeader.getChildren().addAll(quizzesLabel, addQuizButton);

        section.getChildren().addAll(sectionHeader, quizzesList);
        return section;
    }

    @FXML
    private void handleAddLesson(Course course, VBox lessonsList) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/hr/javafx/onlinelearningsystem/fxml/teacher/AddLesson.fxml"));
            Parent root = loader.load();
            AddLessonController controller = loader.getController();
            controller.setCourse(course);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(TITLE);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(coursesContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            refreshLessons(course, lessonsList);
        } catch (IOException e) {
            LOGGER.error("Failed to open add lesson dialog for course '{}'", course.getTitle(), e);
            showError("Failed to open add lesson screen" + e.getMessage());
        }
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

            Button editButton = new Button("Edit");
            editButton.getStyleClass().add(SECONDARY_BUTTON);
            editButton.setOnAction(e -> handleEditLesson(lesson, course, lessonsList));

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("danger-button");
            deleteButton.setOnAction(e -> handleDeleteLesson(lesson, course, lessonsList));

            lessonItem.getChildren().addAll(bullet, lessonLabel, editButton, deleteButton);
            lessonsList.getChildren().add(lessonItem);
        }
    }

    private void handleEditLesson(Lesson lesson, Course course, VBox lessonsList) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/hr/javafx/onlinelearningsystem/fxml/teacher/EditLesson.fxml"));
            Parent root = loader.load();
            EditLessonController controller = loader.getController();
            controller.setLesson(lesson);
            controller.setCourse(course);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(TITLE);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(coursesContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            refreshLessons(course, lessonsList);

        } catch (IOException e) {
            LOGGER.error("Failed to open edit lesson dialog for lesson '{}'", lesson.getTitle(), e);
            showError("Failed to open edit lesson screen: " + e.getMessage());
        }
    }

    private void handleDeleteLesson(Lesson lesson, Course course, VBox lessonsList) {

        Optional<ButtonType> result = showConfirmation("Are you sure you want to delete this lesson?");

        if(result.isPresent() && result.get() == ButtonType.OK) {
            lessonRepository.delete(lesson.getId());
            showInformation("Lesson successfully removed");
        }

        refreshLessons(course, lessonsList);

    }

    private void refreshLessons(Course course, VBox lessonsList) {
        Course updatedCourse = courseRepository.findAll()
                .stream()
                .filter(c -> c.getId().equals(course.getId()))
                .findFirst()
                .orElse(course);
        loadLessons(lessonsList, updatedCourse);
    }

    @FXML
    private void handleAddQuiz(Course course, VBox quizzesList) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/hr/javafx/onlinelearningsystem/fxml/teacher/AddQuiz.fxml"));
            Parent root = loader.load();
            AddQuizController controller = loader.getController();
            controller.setCourse(course);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(TITLE);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(coursesContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            refreshQuizzes(course, quizzesList);
        } catch (IOException e) {
            LOGGER.error("Failed to open add quiz dialog for course '{}'", course.getTitle(), e);
            showError("Failed to open add quiz screen " + e.getMessage());
        }
    }

    private void loadQuizzes(VBox quizzesList, Course course) {
        quizzesList.getChildren().clear();

        for (Quiz quiz : quizRepository.findByCourse(course.getId())) {
            HBox quizItem = new HBox(10.0);
            quizItem.setAlignment(Pos.CENTER_LEFT);
            quizItem.getStyleClass().add("quiz-item");

            Label bullet = new Label("•");
            bullet.getStyleClass().add("bullet-point");

            Label quizLabel = new Label(quiz.getTitle());
            quizLabel.getStyleClass().add("item-text");
            HBox.setHgrow(quizLabel, Priority.ALWAYS);

            String dueDateText = quiz.getDueDate() == null
                    ? "No due date"
                    : "Due: " + quiz.getDueDate().format(QUIZ_DATE_FORMAT);
            Label dueDateLabel = new Label(dueDateText);
            dueDateLabel.getStyleClass().add("quiz-pending");

            Label stateLabel = new Label(quiz.isReadyToTake() ? "Ready" : "Draft");
            stateLabel.getStyleClass().add(quiz.isReadyToTake() ? "quiz-ready" : "quiz-draft");

            Button manageQuestionsButton = new Button("Manage Questions");
            manageQuestionsButton.getStyleClass().add(SECONDARY_BUTTON);
            manageQuestionsButton.setOnAction(event -> handleManageQuestions(quiz));
            manageQuestionsButton.setDisable(quiz.isReadyToTake());

            Button editButton = new Button("Edit");
            editButton.getStyleClass().add(SECONDARY_BUTTON);
            editButton.setOnAction(event -> handleEditQuiz(quiz, course, quizzesList));
            editButton.setDisable(quiz.isReadyToTake());

            Button publishButton = new Button(quiz.isReadyToTake() ? "Published" : "Mark Ready");
            publishButton.getStyleClass().add(PRIMARY_BUTTON);
            publishButton.setDisable(quiz.isReadyToTake());
            publishButton.setOnAction(event -> handleMarkReady(quiz, course, quizzesList));

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("danger-button");
            deleteButton.setOnAction(event -> handleDeleteQuiz(quiz, course, quizzesList));
            deleteButton.setDisable(quiz.isReadyToTake());

            Button viewSubmissionsButton = new Button("View Submissions");
            viewSubmissionsButton.getStyleClass().add(SECONDARY_BUTTON);
            viewSubmissionsButton.setOnAction(event -> handleViewSubmissions(quiz));

            quizItem.getChildren().addAll(
                    bullet,
                    quizLabel,
                    dueDateLabel,
                    stateLabel,
                    publishButton,
                    manageQuestionsButton,
                    editButton,
                    deleteButton,
                    viewSubmissionsButton
            );
            quizzesList.getChildren().add(quizItem);
        }
    }

    private void handleMarkReady(Quiz quiz, Course course, VBox quizzesList) {
        if (quiz.isReadyToTake()) {
            showInformation("Quiz is already ready to take.");
            return;
        }

        if (questionRepository.findByQuiz(quiz.getId()).isEmpty()) {
            showWarning("Add at least one question before marking the quiz as ready.");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
                "Mark quiz \"" + quiz.getTitle() + "\" as ready to take? After this, quiz details and questions cannot be modified."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            quizRepository.markReady(quiz.getId());
            showInformation("Quiz is now ready to take.");
            refreshQuizzes(course, quizzesList);
        }
    }

    private void refreshQuizzes(Course course, VBox quizzesList) {
        Course updatedCourse = courseRepository.findAll()
                .stream()
                .filter(c -> c.getId().equals(course.getId()))
                .findFirst()
                .orElse(course);
        loadQuizzes(quizzesList, updatedCourse);
    }

    private void handleManageQuestions(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/hr/javafx/onlinelearningsystem/fxml/teacher/ManageQuestions.fxml"));
            Parent root = loader.load();
            ManageQuestionsController controller = loader.getController();
            controller.setQuiz(quiz);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(TITLE + " - " + quiz.getTitle());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(coursesContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(true);
            dialogStage.showAndWait();
        } catch (IOException e) {
            LOGGER.error("Failed to open manage questions dialog for quiz '{}'", quiz.getTitle(), e);
            showError("Failed to open manage questions screen " + e.getMessage());
        }
    }

    private void handleEditQuiz(Quiz quiz, Course course, VBox quizzesList) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/hr/javafx/onlinelearningsystem/fxml/teacher/EditQuiz.fxml"));
            Parent root = loader.load();
            EditQuizController controller = loader.getController();
            controller.setQuiz(quiz);
            controller.setCourse(course);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(TITLE);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(coursesContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            refreshQuizzes(course, quizzesList);
        } catch (IOException e) {
            LOGGER.error("Failed to open edit quiz dialog for quiz '{}'", quiz.getTitle(), e);
            showError("Failed to open edit quiz screen " + e.getMessage());
        }
    }

    private void handleViewSubmissions(Quiz quiz) {
        List<QuizSubmission> submissions = submissionRepository.findAll().stream()
                .filter(submission -> submission.quiz() != null)
                .filter(submission -> quiz.getId().equals(submission.quiz().getId()))
                .sorted(Comparator.comparing(QuizSubmission::submittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        if (submissions.isEmpty()) {
            showInformation("No submissions available for quiz \"" + quiz.getTitle() + "\" yet.");
            return;
        }

        Map<Long, String> studentsByEnrolmentId = enrollmentRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Entity::getId,
                        enrolment -> enrolment.getStudent() == null
                                ? UNKNOWN
                                : enrolment.getStudent().getFirstName() + " " + enrolment.getStudent().getLastName(),
                        (left, right) -> left
                ));

        TableView<QuizSubmission> submissionsTable = new TableView<>();
        submissionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<QuizSubmission, String> studentColumn = new TableColumn<>("Student");
        studentColumn.setCellValueFactory(cellData -> {
            Long enrolmentId = cellData.getValue().enrolledStudent().getId();
            return new SimpleObjectProperty<>(studentsByEnrolmentId.getOrDefault(enrolmentId, UNKNOWN));
        });

        TableColumn<QuizSubmission, String> submittedAtColumn = new TableColumn<>("Submitted At");
        submittedAtColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(formatSubmissionDate(cellData.getValue().submittedAt())));

        TableColumn<QuizSubmission, String> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(formatSubmissionGrade(cellData.getValue())));

        TableColumn<QuizSubmission, Void> reviewColumn = new TableColumn<>("Review");
        reviewColumn.setCellFactory(ignored -> new TableCell<>() {
            private final Button reviewButton = new Button("Review Test");

            {
                reviewButton.getStyleClass().add(SECONDARY_BUTTON);
                reviewButton.setOnAction(event -> {
                    QuizSubmission submission = getTableView().getItems().get(getIndex());
                    Long enrolmentId = submission.enrolledStudent().getId();
                    String studentName = studentsByEnrolmentId.getOrDefault(enrolmentId, UNKNOWN);
                    handleReviewSubmission(quiz, submission, studentName);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : reviewButton);
            }
        });

        submissionsTable.getColumns().addAll(
                List.of(studentColumn, submittedAtColumn, gradeColumn, reviewColumn)
        );
        submissionsTable.getItems().setAll(submissions);

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add(SECONDARY_BUTTON);

        HBox footer = new HBox(closeButton);
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12.0);
        root.setPadding(new Insets(16.0));
        Label titleLabel = new Label("Submissions - " + quiz.getTitle());
        titleLabel.getStyleClass().add("pageTitle-label");
        root.getChildren().addAll(titleLabel, submissionsTable, footer);

        Stage dialogStage = new Stage();
        dialogStage.setTitle(TITLE);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(coursesContainer.getScene().getWindow());

        Scene scene = new Scene(root, 700, 420);
        scene.getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource("/hr/javafx/onlinelearningsystem/styles.css")).toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.setResizable(true);

        closeButton.setOnAction(event -> dialogStage.close());
        dialogStage.showAndWait();
    }

    private void handleReviewSubmission(Quiz quiz, QuizSubmission submission, String studentName) {
        VBox content = new VBox(12.0);
        content.setPadding(new Insets(16.0));

        Label titleLabel = new Label("Review - " + quiz.getTitle());
        titleLabel.getStyleClass().add("pageTitle-label");

        Label metaLabel = new Label(
                "Student: " + studentName
                        + " | Submitted: " + formatSubmissionDate(submission.submittedAt())
                        + " | Grade: " + formatSubmissionGrade(submission)
        );
        metaLabel.getStyleClass().add(STAT_LABEL);

        Map<Integer, Integer> selectedAnswers = Optional.ofNullable(submission.selectedAnswers()).orElse(Map.of());
        if (selectedAnswers.isEmpty()) {
            Label noAnswerDetailsLabel = new Label("Selected answers are not available for this submission.");
            noAnswerDetailsLabel.getStyleClass().add(STAT_LABEL);
            content.getChildren().add(noAnswerDetailsLabel);
        }

        VBox questionsBox = new VBox(10.0);
        for (Question question : questionRepository.findByQuiz(quiz.getId())) {
            VBox questionCard = new VBox(6.0);
            questionCard.getStyleClass().add("quiz-question-card");
            questionCard.setPadding(new Insets(12.0));

            Integer correctIndex = question.correctAnswerIndex();
            Integer selectedIndex = selectedAnswers.get(question.id());

            HBox questionHeader = new HBox(8.0);
            questionHeader.setAlignment(Pos.CENTER_LEFT);

            Label questionTitle = new Label(question.text());
            questionTitle.getStyleClass().add("quiz-question-title");
            questionTitle.setWrapText(true);

            Label questionStatus = new Label();
            if (selectedIndex == null || correctIndex == null) {
                questionStatus.setText("?");
                questionStatus.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: bold;");
            } else if (selectedIndex.equals(correctIndex)) {
                questionStatus.setText("OK");
                questionStatus.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            } else {
                questionStatus.setText("X");
                questionStatus.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
            }

            questionHeader.getChildren().addAll(questionTitle, questionStatus);
            questionCard.getChildren().add(questionHeader);

            List<String> options = question.options();
            for (int i = 0; i < options.size(); i++) {
                boolean isSelected = selectedIndex != null && i == selectedIndex;
                boolean isCorrect = correctIndex != null && i == correctIndex;

                String prefix;
                if (isSelected && isCorrect) {
                    prefix = "[SELECTED + CORRECT] ";
                } else if (isSelected) {
                    prefix = "[SELECTED] ";
                } else if (isCorrect) {
                    prefix = "[CORRECT] ";
                } else {
                    prefix = "[ ] ";
                }

                Label optionLabel = new Label(prefix + options.get(i));
                optionLabel.getStyleClass().add("quiz-option");
                optionLabel.setWrapText(true);
                questionCard.getChildren().add(optionLabel);
            }

            questionsBox.getChildren().add(questionCard);
        }

        if (questionsBox.getChildren().isEmpty()) {
            Label empty = new Label("This quiz has no questions.");
            empty.getStyleClass().add(STAT_LABEL);
            questionsBox.getChildren().add(empty);
        }

        ScrollPane scrollPane = new ScrollPane(questionsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add(SECONDARY_BUTTON);
        HBox footer = new HBox(closeButton);
        footer.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(titleLabel, metaLabel, scrollPane, footer);

        Stage dialogStage = new Stage();
        dialogStage.setTitle(TITLE);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(coursesContainer.getScene().getWindow());

        Scene scene = new Scene(content, 760, 560);
        scene.getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource("/hr/javafx/onlinelearningsystem/styles.css")).toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.setResizable(true);

        closeButton.setOnAction(event -> dialogStage.close());
        dialogStage.showAndWait();
    }

    private String formatSubmissionDate(LocalDateTime submittedAt) {
        if (submittedAt == null) {
            return "-";
        }
        return submittedAt.format(SUBMISSION_DATE_FORMAT);
    }

    private String formatSubmissionGrade(QuizSubmission submission) {
        if (submission.grade() == null || submission.percentage() == null) {
            return "Submitted";
        }
        return String.format("%d (%.1f%%)", submission.grade(), submission.percentage());
    }

    private void handleDeleteQuiz(Quiz quiz, Course course, VBox quizzesList) {
        Optional<ButtonType> result = showConfirmation("Are you sure you want to delete quiz \"" + quiz.getTitle() + "\"?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                quizRepository.delete(quiz.getId());
                showInformation("Quiz successfully removed.");
                refreshQuizzes(course, quizzesList);
            } catch (RepositoryException e) {
                LOGGER.error("Failed to delete quiz '{}'", quiz.getTitle(), e);
                showError("Failed to delete quiz: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleShowEnrollments(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/hr/javafx/onlinelearningsystem/fxml/teacher/ShowEnrollments.fxml"));
            Parent root = loader.load();
            ShowEnrollmentsController controller = loader.getController();
            controller.setCourse(course);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(TITLE);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(coursesContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            reloadCourseCards();
        } catch (IOException e) {
            LOGGER.error("Failed to open enrollments dialog for course '{}'", course.getTitle(), e);
            showError("Failed to open show enrollments screen" + e.getMessage());
        }
    }
}

