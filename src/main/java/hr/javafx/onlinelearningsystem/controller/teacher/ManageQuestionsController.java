package hr.javafx.onlinelearningsystem.controller.teacher;

import hr.javafx.onlinelearningsystem.model.Question;
import hr.javafx.onlinelearningsystem.model.Quiz;
import hr.javafx.onlinelearningsystem.repository.QuestionRepository;
import hr.javafx.onlinelearningsystem.repository.QuizRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static hr.javafx.onlinelearningsystem.controller.LoginController.ERROR_STYLE;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showConfirmation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showInformation;
import static hr.javafx.onlinelearningsystem.util.AlertUtil.showWarning;
import static hr.javafx.onlinelearningsystem.util.SceneManager.closeDialog;

public class ManageQuestionsController {

    @FXML private ListView<Question> questionsList;
    @FXML private TextArea questionTextArea;
    @FXML private TextField optionAField;
    @FXML private TextField optionBField;
    @FXML private TextField optionCField;
    @FXML private TextField optionDField;
    @FXML private ComboBox<String> correctAnswerCombo;
    @FXML private Button deleteButton;
    @FXML private Button newButton;
    @FXML private Button saveButton;
    @FXML private Button closeButton;

    private final QuestionRepository questionRepository = new QuestionRepository();
    private final QuizRepository quizRepository = new QuizRepository();

    private Quiz quiz;
    private Question selectedQuestion;
    private boolean quizLocked;

    private static final String MODIFYWARNING = "This quiz is ready to take and questions can no longer be modified.";

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        this.quizLocked = quizRepository.isReadyToTake(quiz.getId());
        applyLockState();
        if (quizLocked) {
            showWarning(MODIFYWARNING);
        }
        loadQuestions();
    }

    @FXML
    private void initialize() {
        correctAnswerCombo.getItems().setAll("A", "B", "C", "D");
        correctAnswerCombo.getSelectionModel().selectFirst();

        questionsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String preview = item.text();
                    if (preview.length() > 60) {
                        preview = preview.substring(0, 60) + "...";
                    }
                    setText("Q" + item.id() + " - " + preview);
                }
            }
        });

        questionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedQuestion = newValue;
            deleteButton.setDisable(quizLocked || newValue == null);
            if (newValue == null) {
                clearForm();
            } else {
                populateForm(newValue);
            }
        });

        deleteButton.setDisable(true);
    }

    private void loadQuestions() {
        questionsList.getItems().setAll(questionRepository.findByQuiz(quiz.getId()));
        selectedQuestion = null;
        clearForm();
        deleteButton.setDisable(true);
    }

    private void populateForm(Question question) {
        questionTextArea.setText(question.text());

        List<String> options = question.options();
        optionAField.setText(!options.isEmpty() ? options.get(0) : "");
        optionBField.setText(options.size() > 1 ? options.get(1) : "");
        optionCField.setText(options.size() > 2 ? options.get(2) : "");
        optionDField.setText(options.size() > 3 ? options.get(3) : "");

        Integer correctIndex = question.correctAnswerIndex();
        if (correctIndex != null && correctIndex >= 0 && correctIndex < 4) {
            correctAnswerCombo.getSelectionModel().select(correctIndex);
        } else {
            correctAnswerCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleNew() {
        if (quizLocked) {
            showWarning(MODIFYWARNING);
            return;
        }
        questionsList.getSelectionModel().clearSelection();
        selectedQuestion = null;
        clearForm();
        deleteButton.setDisable(true);
    }

    @FXML
    private void handleSave() {
        if (quizLocked) {
            showWarning(MODIFYWARNING);
            return;
        }

        if (!validateInput()) {
            return;
        }

        List<String> options = new ArrayList<>();
        options.add(optionAField.getText().trim());
        options.add(optionBField.getText().trim());
        options.add(optionCField.getText().trim());
        options.add(optionDField.getText().trim());

        Integer correctAnswerIndex = correctAnswerCombo.getSelectionModel().getSelectedIndex();

        if (selectedQuestion == null) {
            Question question = new Question(
                    null,
                    questionTextArea.getText().trim(),
                    quiz,
                    options,
                    correctAnswerIndex
            );
            questionRepository.save(question);
            showInformation("Question added successfully!");
        } else {
            Question question = new Question(
                    selectedQuestion.id(),
                    questionTextArea.getText().trim(),
                    quiz,
                    options,
                    correctAnswerIndex
            );
            questionRepository.update(question);
            showInformation("Question updated successfully!");
        }

        loadQuestions();
    }

    @FXML
    private void handleDelete() {
        if (quizLocked) {
            showWarning(MODIFYWARNING);
            return;
        }

        if (selectedQuestion == null) {
            showWarning("Please select a question to delete.");
            return;
        }

        Optional<javafx.scene.control.ButtonType> result = showConfirmation("Delete selected question?");

        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            questionRepository.delete(selectedQuestion.id());
            showInformation("Question removed successfully!");
            loadQuestions();
        }
    }

    private boolean validateInput() {
        boolean valid = true;
        StringBuilder message = new StringBuilder();

        clearErrorStyles();

        if (questionTextArea.getText().isBlank()) {
            questionTextArea.getStyleClass().add(ERROR_STYLE);
            message.append("Please enter question text.\n");
            valid = false;
        }

        if (optionAField.getText().isBlank()) {
            optionAField.getStyleClass().add(ERROR_STYLE);
            message.append("Option A is required.\n");
            valid = false;
        }

        if (optionBField.getText().isBlank()) {
            optionBField.getStyleClass().add(ERROR_STYLE);
            message.append("Option B is required.\n");
            valid = false;
        }

        if (optionCField.getText().isBlank()) {
            optionCField.getStyleClass().add(ERROR_STYLE);
            message.append("Option C is required.\n");
            valid = false;
        }

        if (optionDField.getText().isBlank()) {
            optionDField.getStyleClass().add(ERROR_STYLE);
            message.append("Option D is required.\n");
            valid = false;
        }

        if (correctAnswerCombo.getSelectionModel().getSelectedItem() == null) {
            correctAnswerCombo.getStyleClass().add(ERROR_STYLE);
            message.append("Please choose the correct answer.\n");
            valid = false;
        }

        if (!valid) {
            showWarning(message.toString().trim());
        }

        return valid;
    }

    private void clearForm() {
        questionTextArea.clear();
        optionAField.clear();
        optionBField.clear();
        optionCField.clear();
        optionDField.clear();
        correctAnswerCombo.getSelectionModel().selectFirst();
        clearErrorStyles();
    }

    private void clearErrorStyles() {
        questionTextArea.getStyleClass().remove(ERROR_STYLE);
        optionAField.getStyleClass().remove(ERROR_STYLE);
        optionBField.getStyleClass().remove(ERROR_STYLE);
        optionCField.getStyleClass().remove(ERROR_STYLE);
        optionDField.getStyleClass().remove(ERROR_STYLE);
        correctAnswerCombo.getStyleClass().remove(ERROR_STYLE);
    }

    private void applyLockState() {
        questionTextArea.setDisable(quizLocked);
        optionAField.setDisable(quizLocked);
        optionBField.setDisable(quizLocked);
        optionCField.setDisable(quizLocked);
        optionDField.setDisable(quizLocked);
        correctAnswerCombo.setDisable(quizLocked);
        newButton.setDisable(quizLocked);
        saveButton.setDisable(quizLocked);
        deleteButton.setDisable(quizLocked || selectedQuestion == null);
    }

    @FXML
    private void handleClose() {
        Window owner = closeButton.getScene().getWindow();
        closeDialog(owner);
    }
}

