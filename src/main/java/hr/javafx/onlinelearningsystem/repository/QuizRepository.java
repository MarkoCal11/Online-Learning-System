package hr.javafx.onlinelearningsystem.repository;

import hr.javafx.onlinelearningsystem.exception.EntityNotFoundException;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Quiz;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static hr.javafx.onlinelearningsystem.util.DBConnection.connectToDatabase;

public class QuizRepository {

    public List<Quiz> findAll() {
        List<Quiz> quizzes = new ArrayList<>();
        String query = "SELECT * FROM QUIZ";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                quizzes.add(getEntityFromResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }

        return quizzes;
    }

    public List<Quiz> findByCourse(Long courseId) {
        List<Quiz> quizzes = new ArrayList<>();
        String query = "SELECT * FROM QUIZ WHERE COURSE_ID = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, courseId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    quizzes.add(getEntityFromResultSet(resultSet));
                }
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }

        return quizzes;
    }

    public Quiz findById(Integer id) {
        String query = "SELECT * FROM QUIZ WHERE ID = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return getEntityFromResultSet(resultSet);
                }
                throw new EntityNotFoundException();
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public Quiz getEntityFromResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String title = resultSet.getString("TITLE");
        LocalDate dueDate = resultSet.getObject("DATE", LocalDate.class);
        Long courseId = resultSet.getLong("COURSE_ID");
        boolean readyToTake = resultSet.getBoolean("READY_TO_TAKE");

        Course course = new Course(courseId, null, null, null, null, null, null, null);
        Quiz.Builder builder = new Quiz.Builder(id, title, course);

        if (dueDate != null) {
            builder.onDate(dueDate);
        }

        builder.readyToTake(readyToTake);

        return builder.build();
    }

    public void saveAll(List<Quiz> quizzes) {
        String query = "INSERT INTO QUIZ (TITLE, DATE, COURSE_ID, READY_TO_TAKE) VALUES (?, ?, ?, ?)";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (Quiz quiz : quizzes) {
                preparedStatement.setString(1, quiz.getTitle());
                preparedStatement.setObject(2,
                        quiz.getDueDate() == null ? null : Date.valueOf(quiz.getDueDate()),
                        Types.DATE);
                preparedStatement.setLong(3, quiz.getCourse().getId());
                preparedStatement.setBoolean(4, quiz.isReadyToTake());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void save(Quiz quiz) {
        List<Quiz> quizzes = new ArrayList<>();
        quizzes.add(quiz);
        saveAll(quizzes);
    }

    public void delete(Long id) {
        if (isReadyToTake(id)) {
            throw new RepositoryException("Quiz is ready to take and can no longer be modified.");
        }

        try (Connection connection = connectToDatabase()) {
            connection.setAutoCommit(false);
            executeQuizDeleteTransaction(connection, id);
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    private void executeQuizDeleteTransaction(Connection connection, Long id) throws SQLException {
        try {
            deleteQuizCascade(connection, id);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    private void deleteQuizCascade(Connection connection, Long id) throws SQLException {
        String deleteSubmissionAnswersQuery = """
                DELETE FROM QUIZ_SUBMISSION_ANSWER
                WHERE QUIZ_SUBMISSION_ID IN (SELECT ID FROM QUIZ_SUBMISSION WHERE QUIZ_ID = ?)
                """;
        String deleteSubmissionsQuery = "DELETE FROM QUIZ_SUBMISSION WHERE QUIZ_ID = ?";
        String deleteQuestionOptionsQuery = """
                DELETE FROM QUESTION_OPTION
                WHERE QUESTION_ID IN (SELECT ID FROM QUESTION WHERE QUIZ_ID = ?)
                """;
        String deleteQuestionsQuery = "DELETE FROM QUESTION WHERE QUIZ_ID = ?";
        String deleteQuizQuery = "DELETE FROM QUIZ WHERE ID = ?";

        try (PreparedStatement deleteSubmissionAnswersStatement = connection.prepareStatement(deleteSubmissionAnswersQuery);
             PreparedStatement deleteSubmissionsStatement = connection.prepareStatement(deleteSubmissionsQuery);
             PreparedStatement deleteQuestionOptionsStatement = connection.prepareStatement(deleteQuestionOptionsQuery);
             PreparedStatement deleteQuestionsStatement = connection.prepareStatement(deleteQuestionsQuery);
             PreparedStatement deleteQuizStatement = connection.prepareStatement(deleteQuizQuery)) {

            deleteSubmissionAnswersStatement.setLong(1, id);
            deleteSubmissionAnswersStatement.executeUpdate();

            deleteSubmissionsStatement.setLong(1, id);
            deleteSubmissionsStatement.executeUpdate();

            deleteQuestionOptionsStatement.setLong(1, id);
            deleteQuestionOptionsStatement.executeUpdate();

            deleteQuestionsStatement.setLong(1, id);
            deleteQuestionsStatement.executeUpdate();

            deleteQuizStatement.setLong(1, id);
            deleteQuizStatement.executeUpdate();
        }
    }

    public void update(Quiz quiz, Long id) {
        if (isReadyToTake(id)) {
            throw new RepositoryException("Quiz is ready to take and can no longer be modified.");
        }

        String query = """
                UPDATE QUIZ
                SET TITLE = ?, DATE = ?, READY_TO_TAKE = ?
                WHERE ID = ?
                """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, quiz.getTitle());
            if (quiz.getDueDate() != null) {
                preparedStatement.setDate(2, Date.valueOf(quiz.getDueDate()));
            } else {
                preparedStatement.setNull(2, Types.DATE);
            }
            preparedStatement.setBoolean(3, quiz.isReadyToTake());
            preparedStatement.setLong(4, id);
            preparedStatement.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public boolean isReadyToTake(Long quizId) {
        String query = "SELECT READY_TO_TAKE FROM QUIZ WHERE ID = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, quizId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("READY_TO_TAKE");
                }
                throw new EntityNotFoundException();
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void markReady(Long quizId) {
        String query = "UPDATE QUIZ SET READY_TO_TAKE = TRUE WHERE ID = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, quizId);
            preparedStatement.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }
}

