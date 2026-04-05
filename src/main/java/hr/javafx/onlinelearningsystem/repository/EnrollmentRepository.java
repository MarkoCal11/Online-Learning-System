package hr.javafx.onlinelearningsystem.repository;

import java.sql.*;
import hr.javafx.onlinelearningsystem.enums.EnrolmentStatus;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Enrolment;
import hr.javafx.onlinelearningsystem.model.StudentProfile;
import hr.javafx.onlinelearningsystem.util.DBConnection;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static hr.javafx.onlinelearningsystem.util.DBConnection.connectToDatabase;

public class EnrollmentRepository {

    private final StudentRepository studentRepository = new StudentRepository();
    private final CourseRepository courseRepository = new CourseRepository();


    public List<Enrolment> findAll(){

        List<Enrolment> enrolments = new ArrayList<>();

        String query = "SELECT * FROM ENROLMENT";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Enrolment enrolment = getEntityFromResultSet(resultSet);
                enrolments.add(enrolment);
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
        return enrolments;
    }

    public Enrolment getEntityFromResultSet (ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        Integer studentId = resultSet.getObject("STUDENT_ID", Integer.class);
        Integer courseId = resultSet.getObject("COURSE_ID", Integer.class);
        LocalDate enrolmentDate = resultSet.getDate("ENROLMENT_DATE").toLocalDate();
        String enrolmentStatusDB = resultSet.getString("STATUS");
        Double finalGrade = resultSet.getObject("FINAL_GRADE", Double.class);

        StudentProfile student = studentRepository.findById(studentId);
        Course course = courseRepository.findById(courseId);
        EnrolmentStatus enrolmentStatus = EnrolmentStatus.valueOf(enrolmentStatusDB);
        Enrolment enrolment = new Enrolment(id, student, course, enrolmentDate, enrolmentStatus);
        enrolment.setFinalGrade(finalGrade);

        return enrolment;
    }

    public void enroll(Long studentId, Long courseId) {
        String query = "INSERT INTO ENROLMENT (STUDENT_ID, COURSE_ID, ENROLMENT_DATE, STATUS) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.connectToDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, studentId);
            stmt.setLong(2, courseId);
            stmt.setDate(3, Date.valueOf(LocalDateTime.now().toLocalDate()));
            stmt.setString(4, String.valueOf(EnrolmentStatus.ACTIVE));
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void unenroll(Long studentId, Long courseId) {
        String deleteSubmissionAnswersQuery = """
                DELETE FROM QUIZ_SUBMISSION_ANSWER
                WHERE QUIZ_SUBMISSION_ID IN (
                    SELECT ID FROM QUIZ_SUBMISSION
                    WHERE ENROLMENT_ID IN (
                        SELECT ID FROM ENROLMENT WHERE STUDENT_ID = ? AND COURSE_ID = ?
                    )
                )
                """;
        String deleteSubmissionQuery = """
                DELETE FROM QUIZ_SUBMISSION
                WHERE ENROLMENT_ID IN (
                    SELECT ID FROM ENROLMENT WHERE STUDENT_ID = ? AND COURSE_ID = ?
                )
                """;
        String deleteEnrolmentQuery = "DELETE FROM ENROLMENT WHERE STUDENT_ID = ? AND COURSE_ID = ?";

        try (Connection conn = DBConnection.connectToDatabase();
             PreparedStatement deleteSubmissionAnswersStmt = conn.prepareStatement(deleteSubmissionAnswersQuery);
             PreparedStatement deleteSubmissionStmt = conn.prepareStatement(deleteSubmissionQuery);
             PreparedStatement deleteEnrolmentStmt = conn.prepareStatement(deleteEnrolmentQuery)) {

            deleteSubmissionAnswersStmt.setLong(1, studentId);
            deleteSubmissionAnswersStmt.setLong(2, courseId);
            deleteSubmissionAnswersStmt.executeUpdate();

            deleteSubmissionStmt.setLong(1, studentId);
            deleteSubmissionStmt.setLong(2, courseId);
            deleteSubmissionStmt.executeUpdate();

            deleteEnrolmentStmt.setLong(1, studentId);
            deleteEnrolmentStmt.setLong(2, courseId);
            deleteEnrolmentStmt.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void unenrollFromAll(Long studentId) {
        String deleteSubmissionAnswersQuery = """
                DELETE FROM QUIZ_SUBMISSION_ANSWER
                WHERE QUIZ_SUBMISSION_ID IN (
                    SELECT ID FROM QUIZ_SUBMISSION
                    WHERE ENROLMENT_ID IN (
                        SELECT ID FROM ENROLMENT WHERE STUDENT_ID = ?
                    )
                )
                """;
        String deleteSubmissionQuery = """
                DELETE FROM QUIZ_SUBMISSION
                WHERE ENROLMENT_ID IN (
                    SELECT ID FROM ENROLMENT WHERE STUDENT_ID = ?
                )
                """;
        String deleteEnrolmentQuery = "DELETE FROM ENROLMENT WHERE STUDENT_ID = ?";

        try (Connection conn = DBConnection.connectToDatabase();
             PreparedStatement deleteSubmissionAnswersStmt = conn.prepareStatement(deleteSubmissionAnswersQuery);
             PreparedStatement deleteSubmissionStmt = conn.prepareStatement(deleteSubmissionQuery);
             PreparedStatement deleteEnrolmentStmt = conn.prepareStatement(deleteEnrolmentQuery)) {

            deleteSubmissionAnswersStmt.setLong(1, studentId);
            deleteSubmissionAnswersStmt.executeUpdate();

            deleteSubmissionStmt.setLong(1, studentId);
            deleteSubmissionStmt.executeUpdate();

            deleteEnrolmentStmt.setLong(1, studentId);
            deleteEnrolmentStmt.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public List<Long> findStudentIdsByCourse(Long courseId) {
        List<Long> studentIds = new ArrayList<>();
        String query = "SELECT STUDENT_ID FROM ENROLMENT WHERE COURSE_ID = ?";

        try (Connection conn = connectToDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                studentIds.add(rs.getLong("STUDENT_ID"));
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
        return studentIds;
    }

    public Integer countByStudent(Long id) {
        String query = "SELECT COUNT(*) FROM ENROLMENT WHERE STUDENT_ID = ?";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public Integer countStudentsByCourse(Course course) {
        String query = "SELECT COUNT(*) FROM ENROLMENT WHERE COURSE_ID = ?";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, course.getId());
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void updateFinalGrade(Long enrolmentId, Double finalGrade) {
        String query = "UPDATE ENROLMENT SET FINAL_GRADE = ?, STATUS = ? WHERE ID = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            if (finalGrade == null) {
                preparedStatement.setNull(1, Types.DOUBLE);
                preparedStatement.setString(2, EnrolmentStatus.ACTIVE.name());
            } else {
                preparedStatement.setDouble(1, finalGrade);
                preparedStatement.setString(2, EnrolmentStatus.COMPLETED.name());
            }

            preparedStatement.setLong(3, enrolmentId);
            preparedStatement.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

}