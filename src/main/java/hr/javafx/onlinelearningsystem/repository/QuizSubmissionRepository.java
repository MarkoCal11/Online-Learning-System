package hr.javafx.onlinelearningsystem.repository;

import hr.javafx.onlinelearningsystem.enums.EnrolmentStatus;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.model.Enrolment;
import hr.javafx.onlinelearningsystem.model.Quiz;
import hr.javafx.onlinelearningsystem.model.QuizSubmission;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static hr.javafx.onlinelearningsystem.util.DBConnection.connectToDatabase;

public class QuizSubmissionRepository {

    public List<QuizSubmission> findAll() {
        List<QuizSubmission> submissions = new ArrayList<>();
        String query = """
                SELECT ID, QUIZ_ID, ENROLMENT_ID, SUBMITTED_AT, GRADE, PERCENTAGE
                FROM QUIZ_SUBMISSION
                """;

        try (Connection connection = connectToDatabase()) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("ID");
                    Long quizId = resultSet.getLong("QUIZ_ID");
                    Long enrolmentId = resultSet.getLong("ENROLMENT_ID");

                    Timestamp submittedAtRaw = resultSet.getTimestamp("SUBMITTED_AT");
                    LocalDateTime submittedAt = submittedAtRaw == null ? null : submittedAtRaw.toLocalDateTime();

                    Integer grade = resultSet.getInt("GRADE");
                    if (resultSet.wasNull()) {
                        grade = null;
                    }

                    Double percentage = resultSet.getDouble("PERCENTAGE");
                    if (resultSet.wasNull()) {
                        percentage = null;
                    }

                    Quiz quiz = new Quiz.Builder(quizId, null, null).build();
                    Enrolment enrolment = new Enrolment(enrolmentId, null, null, LocalDate.now(), EnrolmentStatus.ACTIVE);
                    Map<Integer, Integer> selectedAnswers = loadSelectedAnswers(connection, id);

                    submissions.add(new QuizSubmission(id, quiz, enrolment, submittedAt, grade, percentage, selectedAnswers));
                }
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }

        return submissions;
    }

    public boolean existsByQuizAndEnrolment(Long quizId, Long enrolmentId) {
        String query = "SELECT COUNT(*) FROM QUIZ_SUBMISSION WHERE QUIZ_ID = ? AND ENROLMENT_ID = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, quizId);
            preparedStatement.setLong(2, enrolmentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public Optional<QuizSubmission> findByQuizAndEnrolment(Long quizId, Long enrolmentId) {
        String query = """
                SELECT ID, SUBMITTED_AT, GRADE, PERCENTAGE
                FROM QUIZ_SUBMISSION
                WHERE QUIZ_ID = ? AND ENROLMENT_ID = ?
                """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, quizId);
            preparedStatement.setLong(2, enrolmentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Integer id = resultSet.getInt("ID");
                Timestamp submittedAtRaw = resultSet.getTimestamp("SUBMITTED_AT");
                LocalDateTime submittedAt = submittedAtRaw == null ? null : submittedAtRaw.toLocalDateTime();
                Integer grade = resultSet.getInt("GRADE");
                if (resultSet.wasNull()) {
                    grade = null;
                }
                Double percentage = resultSet.getDouble("PERCENTAGE");
                if (resultSet.wasNull()) {
                    percentage = null;
                }

                Quiz quiz = new Quiz.Builder(quizId, null, null).build();
                Enrolment enrolment = new Enrolment(enrolmentId, null, null, LocalDate.now(), EnrolmentStatus.ACTIVE);
                Map<Integer, Integer> selectedAnswers = loadSelectedAnswers(connection, id);

                return Optional.of(new QuizSubmission(id, quiz, enrolment, submittedAt, grade, percentage, selectedAnswers));
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void save(QuizSubmission submission) {
        try (Connection connection = connectToDatabase()) {
            saveInTransaction(connection, submission);
        } catch (SQLException | IOException e) {
            throw new RepositoryException("Failed to save quiz submission", e);
        }
    }

    private void saveInTransaction(Connection connection, QuizSubmission submission) throws SQLException {
        connection.setAutoCommit(false);
        try {
            int submissionId = insertSubmission(connection, submission);
            saveSelectedAnswers(connection, submissionId, submission.selectedAnswers());
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }


    private int insertSubmission(Connection connection, QuizSubmission submission) throws SQLException {
        String query = """
            INSERT INTO QUIZ_SUBMISSION (QUIZ_ID, ENROLMENT_ID, SUBMITTED_AT, GRADE, PERCENTAGE)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, submission.quiz().getId());
            ps.setLong(2, submission.enrolledStudent().getId());
            ps.setTimestamp(3, Timestamp.valueOf(submission.submittedAt()));
            ps.setObject(4, submission.grade(), Types.INTEGER);
            ps.setObject(5, submission.percentage(), Types.DOUBLE);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Quiz submission insert did not return generated ID.");
                }
                return keys.getInt(1);
            }
        }
    }


    private Map<Integer, Integer> loadSelectedAnswers(Connection connection, Integer submissionId) throws SQLException {
        if (submissionId == null) {
            return Map.of();
        }

        String query = """
                SELECT QUESTION_ID, SELECTED_OPTION_INDEX
                FROM QUIZ_SUBMISSION_ANSWER
                WHERE QUIZ_SUBMISSION_ID = ?
                """;

        Map<Integer, Integer> selectedAnswers = new HashMap<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, submissionId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Integer questionId = resultSet.getInt("QUESTION_ID");
                    Integer selectedIndex = resultSet.getObject("SELECTED_OPTION_INDEX", Integer.class);
                    selectedAnswers.put(questionId, selectedIndex);
                }
            }
        }

        return selectedAnswers;
    }

    private void saveSelectedAnswers(Connection connection, Integer submissionId, Map<Integer, Integer> selectedAnswers) throws SQLException {
        if (submissionId == null || selectedAnswers == null || selectedAnswers.isEmpty()) {
            return;
        }

        String query = """
                INSERT INTO QUIZ_SUBMISSION_ANSWER (QUIZ_SUBMISSION_ID, QUESTION_ID, SELECTED_OPTION_INDEX)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, submissionId);
            for (Map.Entry<Integer, Integer> entry : selectedAnswers.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }

                preparedStatement.setInt(2, entry.getKey());
                preparedStatement.setObject(3, entry.getValue(), java.sql.Types.INTEGER);

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}

