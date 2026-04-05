package hr.javafx.onlinelearningsystem.controller.student;

import hr.javafx.onlinelearningsystem.model.Enrolment;
import hr.javafx.onlinelearningsystem.model.Question;
import hr.javafx.onlinelearningsystem.model.Quiz;
import hr.javafx.onlinelearningsystem.model.QuizSubmission;
import hr.javafx.onlinelearningsystem.repository.QuestionRepository;
import hr.javafx.onlinelearningsystem.repository.QuizSubmissionRepository;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showConfirmation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;

public class TakeQuizController {

    @FXML private Label quizTitleLabel;
    @FXML private Label quizMetaLabel;
    @FXML private VBox questionsContainer;
    @FXML private Button submitButton;

    private final QuestionRepository questionRepository = new QuestionRepository();
    private final QuizSubmissionRepository submissionRepository = new QuizSubmissionRepository();

    private final Map<Question, ToggleGroup> answerGroups = new HashMap<>();

    private Quiz quiz;
    private Enrolment enrolment;
    private List<Question> questions;
    private boolean submitted;

    public void setContext(Quiz quiz, Enrolment enrolment) {
        this.quiz = quiz;
        this.enrolment = enrolment;
        this.submitted = false;

        quizTitleLabel.setText(quiz.getTitle());
        quizMetaLabel.setText("Choose one answer for each question.");

        loadQuestions();
    }

    public boolean isSubmitted() {
        return submitted;
    }

    private void loadQuestions() {
        questionsContainer.getChildren().clear();
        answerGroups.clear();

        questions = questionRepository.findByQuiz(quiz.getId());
        submitButton.setDisable(questions.isEmpty());

        if (questions.isEmpty()) {
            Label emptyLabel = new Label("This quiz has no questions yet.");
            emptyLabel.getStyleClass().add("stat-label");
            questionsContainer.getChildren().add(emptyLabel);
            return;
        }

        int index = 1;
        for (Question question : questions) {
            VBox questionCard = new VBox(8.0);
            questionCard.getStyleClass().add("quiz-question-card");
            questionCard.setPadding(new Insets(12.0));

            Label questionLabel = new Label(index + ". " + question.text());
            questionLabel.getStyleClass().add("quiz-question-title");
            questionLabel.setWrapText(true);

            ToggleGroup group = new ToggleGroup();
            answerGroups.put(question, group);

            List<String> options = question.options();
            for (int optionIndex = 0; optionIndex < options.size(); optionIndex++) {
                RadioButton optionButton = new RadioButton(options.get(optionIndex));
                optionButton.getStyleClass().add("quiz-option");
                optionButton.setWrapText(true);
                optionButton.setUserData(optionIndex);
                optionButton.setToggleGroup(group);
                questionCard.getChildren().add(optionButton);
            }

            questionCard.getChildren().addFirst(questionLabel);
            questionsContainer.getChildren().add(questionCard);
            index++;
        }
    }

    @FXML
    private void handleSubmit() {
        if (questions.isEmpty()) {
            showWarning("This quiz does not contain any questions.");
            return;
        }

        if (quiz.isExpired()) {
            showWarning("This quiz is expired and can no longer be submitted.");
            Window owner = submitButton.getScene().getWindow();
            closeDialog(owner);
            return;
        }

        if (submissionRepository.existsByQuizAndEnrolment(quiz.getId(), enrolment.getId())) {
            showWarning("You already submitted this quiz.");
            handleClose();
            return;
        }

        for (Question question : questions) {
            if (answerGroups.get(question).getSelectedToggle() == null) {
                showWarning("Please answer all questions before submitting.");
                return;
            }
        }

        Optional<ButtonType> confirmation = showConfirmation("Are you sure you want to submit this quiz? You cannot change answers after submitting.");
        if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
            return;
        }

        int correctAnswers = 0;
        Map<Integer, Integer> selectedAnswers = new HashMap<>();

        for (Question question : questions) {
            Toggle selectedToggle = answerGroups.get(question).getSelectedToggle();
            Integer selectedIndex = (Integer) selectedToggle.getUserData();
            selectedAnswers.put(question.id(), selectedIndex);
            if (question.isCorrectAnswer(selectedIndex)) {
                correctAnswers++;
            }
        }

        double percentage = (correctAnswers * 100.0) / questions.size();
        int grade;

        if(percentage < 50.0) {
            grade = 1;
        }
        else if(percentage >= 50.0 && percentage < 60.0) {
            grade = 2;
        }
        else if(percentage >= 60.0 && percentage < 75.0) {
            grade = 3;
        }
        else if(percentage >= 75.0 && percentage < 90.0) {
            grade = 4;
        }
        else {
            grade = 5;
        }

        QuizSubmission submission = new QuizSubmission(
                null,
                quiz,
                enrolment,
                LocalDateTime.now(),
                grade,
                percentage,
                selectedAnswers
        );
        submissionRepository.save(submission);
        submitted = true;

        showInformation(String.format("Quiz submitted. Score: %d/%d (%.1f%%)",
                correctAnswers,
                questions.size(),
                percentage));

        handleClose();
    }

    @FXML
    private void handleClose() {
        if (!submitted) {
            showWarning("You cannot close this quiz before submitting.");
            return;
        }
        Window owner = submitButton.getScene().getWindow();
        closeDialog(owner);
    }
}