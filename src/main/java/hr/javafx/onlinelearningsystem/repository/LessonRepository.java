package hr.javafx.onlinelearningsystem.repository;

import hr.javafx.onlinelearningsystem.exception.EntityNotFoundException;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.model.Course;
import hr.javafx.onlinelearningsystem.model.Lesson;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static hr.javafx.onlinelearningsystem.util.DBConnection.connectToDatabase;

public class LessonRepository {


    public List<Lesson> findAll(){

        List<Lesson> lessons = new ArrayList<>();

        String query =  "SELECT * FROM LESSON ";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Lesson lesson = getEntityFromResultSet(resultSet);
                lessons.add(lesson);
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
        return lessons;
    }

    public List<Lesson> findByCourse(Long courseId){

        List<Lesson> lessons = new ArrayList<>();

        String query =  "SELECT * FROM LESSON WHERE COURSE_ID = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, courseId);


            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Lesson lesson = getEntityFromResultSet(resultSet);
                    lessons.add(lesson);
                }
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
        return lessons;
    }

    public Lesson findByTitle (String title) {

        String query = "SELECT * FROM LESSON WHERE TITLE = ?";

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

    public Lesson findById (Integer id) {

        String query = "SELECT * FROM LESSON WHERE ID = ?";

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

    public Lesson getEntityFromResultSet (ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String title = resultSet.getString("TITLE");
        String content = resultSet.getString("CONTENT");
        long courseId = resultSet.getLong("COURSE_ID");

        Course course = new Course(courseId, null, null, null, null, null, null, null);

        return new Lesson(id, title, content, course);
    }

    public void saveAll(List<Lesson> lessons) {

        String query = "INSERT INTO LESSON (TITLE, CONTENT, COURSE_ID) VALUES (?, ?, ?)";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (Lesson lesson : lessons){
                preparedStatement.setString(1, lesson.getTitle());
                preparedStatement.setString(2,lesson.getContent());
                preparedStatement.setLong(3, lesson.getCourse().getId());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void save (Lesson lesson) {
        List<Lesson> lessons = new ArrayList<>();
        lessons.add(lesson);
        saveAll(lessons);
    }

    public void delete(Long id) {

        String query = "DELETE FROM LESSON WHERE ID = ?";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();

        } catch (SQLException | IOException e){
            throw new RepositoryException(e);
        }
    }

    public void update(Lesson lesson, Long id) {

        String query = """
                UPDATE LESSON
                SET TITLE = ?, CONTENT = ?
                WHERE ID = ?
                """;

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, lesson.getTitle());
            preparedStatement.setString(2, lesson.getContent());
            preparedStatement.setLong(3, id);
            preparedStatement.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public Integer count() {
        String query = "SELECT COUNT(*) FROM LESSON";

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
