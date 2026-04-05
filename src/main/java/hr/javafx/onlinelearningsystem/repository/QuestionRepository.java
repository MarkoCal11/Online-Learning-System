package hr.javafx.onlinelearningsystem.repository;

import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.model.Question;
import hr.javafx.onlinelearningsystem.model.Quiz;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static hr.javafx.onlinelearningsystem.util.DBConnection.connectToDatabase;

public class QuestionRepository {

    private final QuizRepository quizRepository = new QuizRepository();

    public List<Question> findByQuiz(Long quizId) {
        List<Question> questions = new ArrayList<>();
        List<QuestionRow> questionRows = new ArrayList<>();
        String query = "SELECT ID, TEXT, QUIZ_ID, CORRECT_ANSWER_INDEX FROM QUESTION WHERE QUIZ_ID = ? ORDER BY ID";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, quizId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    questionRows.add(mapQuestionRow(resultSet));
                }
            }

            for (QuestionRow questionRow : questionRows) {
                questions.add(getEntityFromRow(connection, questionRow));
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }

        return questions;
    }

    private QuestionRow mapQuestionRow(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("ID");
        String text = resultSet.getString("TEXT");
        Long quizId = resultSet.getLong("QUIZ_ID");

        Integer correctAnswerIndex = resultSet.getInt("CORRECT_ANSWER_INDEX");
        if (resultSet.wasNull()) {
            correctAnswerIndex = null;
        }

        return new QuestionRow(id, text, quizId, correctAnswerIndex);
    }

    private Question getEntityFromRow(Connection connection, QuestionRow questionRow) throws SQLException {
        Quiz quiz = new Quiz.Builder(questionRow.quizId(), null, null).build();
        List<String> options = loadOptions(connection, questionRow.id());

        return new Question(questionRow.id(), questionRow.text(), quiz, options, questionRow.correctAnswerIndex());
    }

    private List<String> loadOptions(Connection connection, Integer questionId) throws SQLException {
        List<String> options = new ArrayList<>();
        String query = "SELECT TEXT FROM QUESTION_OPTION WHERE QUESTION_ID = ? ORDER BY OPTION_INDEX";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, questionId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    options.add(resultSet.getString("TEXT"));
                }
            }
        }

        return options;
    }

    public void save(Question question) {
        assertQuizIsEditable(question.quiz().getId());

        String questionQuery = "INSERT INTO QUESTION (TEXT, QUIZ_ID, CORRECT_ANSWER_INDEX) VALUES (?, ?, ?)";
        String optionQuery = "INSERT INTO QUESTION_OPTION (QUESTION_ID, OPTION_INDEX, TEXT) VALUES (?, ?, ?)";

        try (Connection connection = connectToDatabase()) {
            connection.setAutoCommit(false);

            try (PreparedStatement questionStatement = connection.prepareStatement(questionQuery, Statement.RETURN_GENERATED_KEYS)) {
                questionStatement.setString(1, question.text());
                questionStatement.setLong(2, question.quiz().getId());
                if (question.correctAnswerIndex() == null) {
                    questionStatement.setNull(3, java.sql.Types.INTEGER);
                } else {
                    questionStatement.setInt(3, question.correctAnswerIndex());
                }
                questionStatement.executeUpdate();

                int questionId;
                try (ResultSet generatedKeys = questionStatement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("Question insert did not return generated ID.");
                    }
                    questionId = generatedKeys.getInt(1);
                }

                saveOptions(connection, optionQuery, questionId, question.options());
            }

            connection.commit();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void update(Question question) {
        assertQuizIsEditable(question.quiz().getId());

        String updateQuestion = "UPDATE QUESTION SET TEXT = ?, CORRECT_ANSWER_INDEX = ? WHERE ID = ?";
        String deleteOptions = "DELETE FROM QUESTION_OPTION WHERE QUESTION_ID = ?";
        String saveOption = "INSERT INTO QUESTION_OPTION (QUESTION_ID, OPTION_INDEX, TEXT) VALUES (?, ?, ?)";

        try (Connection connection = connectToDatabase()) {
            connection.setAutoCommit(false);

            try (PreparedStatement updateQuestionStatement = connection.prepareStatement(updateQuestion);
                 PreparedStatement deleteOptionsStatement = connection.prepareStatement(deleteOptions)) {

                updateQuestionStatement.setString(1, question.text());
                if (question.correctAnswerIndex() == null) {
                    updateQuestionStatement.setNull(2, java.sql.Types.INTEGER);
                } else {
                    updateQuestionStatement.setInt(2, question.correctAnswerIndex());
                }
                updateQuestionStatement.setInt(3, question.id());
                updateQuestionStatement.executeUpdate();

                deleteOptionsStatement.setInt(1, question.id());
                deleteOptionsStatement.executeUpdate();

                saveOptions(connection, saveOption, question.id(), question.options());
            }

            connection.commit();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    private void saveOptions(Connection connection, String query, Integer questionId, List<String> options) throws SQLException {
        try (PreparedStatement optionStatement = connection.prepareStatement(query)) {
            optionStatement.setInt(1, questionId);
            for (int i = 0; i < options.size(); i++) {
                optionStatement.setInt(2, i);
                optionStatement.setString(3, options.get(i));
                optionStatement.addBatch();
            }
            optionStatement.executeBatch();
        }
    }

    public void delete(Integer id) {
        String deleteOptions = "DELETE FROM QUESTION_OPTION WHERE QUESTION_ID = ?";
        String deleteQuestion = "DELETE FROM QUESTION WHERE ID = ?";
        String quizIdQuery = "SELECT QUIZ_ID FROM QUESTION WHERE ID = ?";

        try (Connection connection = connectToDatabase()) {
            connection.setAutoCommit(false);

            long quizId;
            try (PreparedStatement quizIdStatement = connection.prepareStatement(quizIdQuery)) {
                quizIdStatement.setInt(1, id);
                try (ResultSet resultSet = quizIdStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("Question not found for ID: " + id);
                    }
                    quizId = resultSet.getLong("QUIZ_ID");
                }
            }

            assertQuizIsEditable(quizId);

            try (PreparedStatement deleteOptionsStatement = connection.prepareStatement(deleteOptions);
                 PreparedStatement deleteQuestionStatement = connection.prepareStatement(deleteQuestion)) {

                deleteOptionsStatement.setInt(1, id);
                deleteOptionsStatement.executeUpdate();

                deleteQuestionStatement.setInt(1, id);
                deleteQuestionStatement.executeUpdate();
            }

            connection.commit();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    private void assertQuizIsEditable(Long quizId) {
        if (quizRepository.isReadyToTake(quizId)) {
            throw new RepositoryException("Quiz is ready to take and questions can no longer be modified.");
        }
    }

    private record QuestionRow(Integer id, String text, Long quizId, Integer correctAnswerIndex) {
    }
}

