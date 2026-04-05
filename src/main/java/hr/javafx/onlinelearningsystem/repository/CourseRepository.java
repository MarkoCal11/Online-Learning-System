package hr.javafx.onlinelearningsystem.repository;

import java.sql.*;
import hr.javafx.onlinelearningsystem.model.*;
import hr.javafx.onlinelearningsystem.exception.EntityNotFoundException;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static hr.javafx.onlinelearningsystem.util.DBConnection.connectToDatabase;

public class CourseRepository {

    private final TeacherRepository teacherRepository = new TeacherRepository();

    public List<Course> findAll(){

        List<Course> courses = new ArrayList<>();

        String query =  """
                        SELECT c.*,
                                (SELECT COUNT(*) FROM LESSON l WHERE l.COURSE_ID = c.ID) AS LESSON_COUNT,
                                (SELECT COUNT(*) FROM ENROLMENT e WHERE e.COURSE_ID = c.ID) AS STUDENT_COUNT,
                                (SELECT COUNT(*) FROM QUIZ q WHERE q.COURSE_ID = c.ID) AS QUIZ_COUNT
                        FROM COURSE c 
                        """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Course course = getEntityFromResultSet(resultSet);
                courses.add(course);
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
        return courses;
    }

    public Course findByTitle (String title) {

        String query = """
                        SELECT c.*,
                                (SELECT COUNT(*) FROM LESSON l WHERE l.COURSE_ID = c.ID) AS LESSON_COUNT,
                                (SELECT COUNT(*) FROM ENROLMENT e WHERE e.COURSE_ID = c.ID) AS STUDENT_COUNT,
                                (SELECT COUNT(*) FROM QUIZ q WHERE q.COURSE_ID = c.ID) AS QUIZ_COUNT
                        FROM COURSE c WHERE TITLE = ?
                        """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, title);

            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()) return getEntityFromResultSet(resultSet);
                throw new EntityNotFoundException();
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public Course findById (Integer id) {

        String query = """
                        SELECT c.*,
                                (SELECT COUNT(*) FROM LESSON l WHERE l.COURSE_ID = c.ID) AS LESSON_COUNT,
                                (SELECT COUNT(*) FROM ENROLMENT e WHERE e.COURSE_ID = c.ID) AS STUDENT_COUNT,
                                (SELECT COUNT(*) FROM QUIZ q WHERE q.COURSE_ID = c.ID) AS QUIZ_COUNT
                        FROM COURSE c WHERE ID = ?
                        """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()) return getEntityFromResultSet(resultSet);
                throw new EntityNotFoundException();
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public Course findByStudent (Integer studentId) {

        String query = """
                        SELECT c.*,
                                (SELECT COUNT(*) FROM LESSON l WHERE l.COURSE_ID = c.ID) AS LESSON_COUNT,
                                (SELECT COUNT(*) FROM ENROLMENT e WHERE e.COURSE_ID = c.ID) AS STUDENT_COUNT,
                                (SELECT COUNT(*) FROM QUIZ q WHERE q.COURSE_ID = c.ID) AS QUIZ_COUNT
                        FROM COURSE c WHERE STUDENT_ID = ?
                        """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()) return getEntityFromResultSet(resultSet);
                throw new EntityNotFoundException();
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public Course getEntityFromResultSet (ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String title = resultSet.getString("TITLE");
        String description = resultSet.getString("DESCRIPTION");
        Integer ects = resultSet.getInt("ECTS");
        Integer teacherId = resultSet.getObject("TEACHER_ID", Integer.class);

        TeacherProfile teacher = (teacherId == null) ? null : teacherRepository.findById(teacherId);

        Integer lessonCount = resultSet.getInt("LESSON_COUNT");
        Integer studentCount = resultSet.getInt("STUDENT_COUNT");
        Integer quizCount = resultSet.getInt("QUIZ_COUNT");
        return new Course(id, title, description, ects, teacher, studentCount, lessonCount, quizCount);
    }

    public void saveAll(List<Course> courses) {

        String query = "INSERT INTO COURSE (TITLE, DESCRIPTION, ECTS) VALUES (?, ?, ?)";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (Course course : courses){
                preparedStatement.setString(1, course.getTitle());
                preparedStatement.setString(2,course.getDescription());
                preparedStatement.setInt(3, course.getEcts());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void save (Course course) {
        List<Course> courses = new ArrayList<>();
        courses.add(course);
        saveAll(courses);
    }

    public void addTeacherToCourse(Course course, TeacherProfile teacher) {

        String query = "UPDATE COURSE SET TEACHER_ID = ? WHERE TITLE = ?";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            if(teacher != null) {
                preparedStatement.setInt(1, Math.toIntExact(teacher.getId()));
            }
            else {
                preparedStatement.setNull(1, Types.INTEGER);
            }

            preparedStatement.setString(2, course.getTitle());

            preparedStatement.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void delete(Long courseId) {
        try (Connection connection = connectToDatabase()) {
            connection.setAutoCommit(false);
            executeCourseDeleteTransaction(connection, courseId);
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void delete(String title) {
        Course course = findByTitle(title);
        delete(course.getId());
    }

    private void executeCourseDeleteTransaction(Connection connection, Long courseId) throws SQLException {
        try {
            deleteCourseCascade(connection, courseId);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    private void deleteCourseCascade(Connection connection, Long courseId) throws SQLException {
        String deleteSubmissionAnswersQuery = """
                DELETE FROM QUIZ_SUBMISSION_ANSWER
                WHERE QUIZ_SUBMISSION_ID IN (
                    SELECT qs.ID
                    FROM QUIZ_SUBMISSION qs
                    JOIN QUIZ q ON qs.QUIZ_ID = q.ID
                    WHERE q.COURSE_ID = ?
                )
                """;
        String deleteQuizSubmissionsQuery = """
                DELETE FROM QUIZ_SUBMISSION
                WHERE QUIZ_ID IN (SELECT ID FROM QUIZ WHERE COURSE_ID = ?)
                """;
        String deleteQuestionOptionsQuery = """
                DELETE FROM QUESTION_OPTION
                WHERE QUESTION_ID IN (
                    SELECT qn.ID
                    FROM QUESTION qn
                    JOIN QUIZ q ON qn.QUIZ_ID = q.ID
                    WHERE q.COURSE_ID = ?
                )
                """;
        String deleteQuestionsQuery = """
                DELETE FROM QUESTION
                WHERE QUIZ_ID IN (SELECT ID FROM QUIZ WHERE COURSE_ID = ?)
                """;
        String deleteQuizzesQuery = "DELETE FROM QUIZ WHERE COURSE_ID = ?";
        String deleteLessonsQuery = "DELETE FROM LESSON WHERE COURSE_ID = ?";
        String deleteEnrollmentsQuery = "DELETE FROM ENROLMENT WHERE COURSE_ID = ?";
        String deleteCourseQuery = "DELETE FROM COURSE WHERE ID = ?";

        try (PreparedStatement deleteSubmissionAnswersStatement = connection.prepareStatement(deleteSubmissionAnswersQuery);
             PreparedStatement deleteQuizSubmissionsStatement = connection.prepareStatement(deleteQuizSubmissionsQuery);
             PreparedStatement deleteQuestionOptionsStatement = connection.prepareStatement(deleteQuestionOptionsQuery);
             PreparedStatement deleteQuestionsStatement = connection.prepareStatement(deleteQuestionsQuery);
             PreparedStatement deleteQuizzesStatement = connection.prepareStatement(deleteQuizzesQuery);
             PreparedStatement deleteLessonsStatement = connection.prepareStatement(deleteLessonsQuery);
             PreparedStatement deleteEnrollmentsStatement = connection.prepareStatement(deleteEnrollmentsQuery);
             PreparedStatement deleteCourseStatement = connection.prepareStatement(deleteCourseQuery)) {

            deleteSubmissionAnswersStatement.setLong(1, courseId);
            deleteSubmissionAnswersStatement.executeUpdate();

            deleteQuizSubmissionsStatement.setLong(1, courseId);
            deleteQuizSubmissionsStatement.executeUpdate();

            deleteQuestionOptionsStatement.setLong(1, courseId);
            deleteQuestionOptionsStatement.executeUpdate();

            deleteQuestionsStatement.setLong(1, courseId);
            deleteQuestionsStatement.executeUpdate();

            deleteQuizzesStatement.setLong(1, courseId);
            deleteQuizzesStatement.executeUpdate();

            deleteLessonsStatement.setLong(1, courseId);
            deleteLessonsStatement.executeUpdate();

            deleteEnrollmentsStatement.setLong(1, courseId);
            deleteEnrollmentsStatement.executeUpdate();

            deleteCourseStatement.setLong(1, courseId);
            deleteCourseStatement.executeUpdate();
        }
    }

    public void update(Course course, String oldTitle) {

        String query = """
                UPDATE COURSE
                SET TITLE = ?, DESCRIPTION = ?, ECTS = ?
                WHERE TITLE = ?
                """;

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, course.getTitle());
            preparedStatement.setString(2, course.getDescription());
            preparedStatement.setInt(3, course.getEcts());
            preparedStatement.setString(4, oldTitle);
            preparedStatement.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public Integer countCourses() {
        String query = "SELECT COUNT(*) FROM COURSE";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            if(resultSet.next()){
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public Integer countCoursesByTeacher(Long id) {
        String query = "SELECT COUNT(*) FROM COURSE WHERE TEACHER_ID = ?";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query);) {

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

    public Integer countEnrollments() {
        String query = "SELECT COUNT(*) FROM ENROLMENT";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            if(resultSet.next()){
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }
}