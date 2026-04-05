package hr.javafx.onlinelearningsystem.repository;

import hr.javafx.onlinelearningsystem.exception.EntityNotFoundException;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.model.TeacherProfile;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static hr.javafx.onlinelearningsystem.util.DBConnection.connectToDatabase;

public class TeacherRepository implements UserRepository<TeacherProfile> {

    @Override
    public List<TeacherProfile> findAll(){

        List<TeacherProfile> teachers = new ArrayList<>();

        String query =  """
            SELECT tp.*, COUNT(c.ID) as courses_count,
            EXISTS(SELECT 1
                   FROM PASSWORD_RESET_REQUESTS R 
                   WHERE R.TEACHER_USERNAME = tp.USERNAME
                   AND R.STATUS = 'PENDING') AS HAS_REQUEST
            FROM TEACHER tp
            LEFT JOIN COURSE c ON tp.ID = c.TEACHER_ID
            GROUP BY tp.ID
        """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                TeacherProfile teacher = getEntityFromResultSet(resultSet);
                teachers.add(teacher);
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
        return teachers;
    }

    @Override
    public TeacherProfile findByUsername (String username) {

        String query = """
             SELECT tp.*,
             (SELECT COUNT(*) FROM COURSE c WHERE c.TEACHER_ID = tp.ID) AS courses_count,
             EXISTS(SELECT 1 
                    FROM PASSWORD_RESET_REQUESTS r 
                    WHERE r.TEACHER_USERNAME = tp.USERNAME 
                    AND r.STATUS = 'PENDING') AS HAS_REQUEST 
             FROM TEACHER tp WHERE tp.USERNAME = ?
        """;


        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()) return getEntityFromResultSet(resultSet);
                throw new EntityNotFoundException();
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public TeacherProfile findById (Integer id) {

        String query = """
             SELECT tp.*,
             (SELECT COUNT(*) FROM COURSE c WHERE c.TEACHER_ID = tp.ID) AS courses_count,
             EXISTS(SELECT 1 
                    FROM PASSWORD_RESET_REQUESTS r 
                    WHERE r.TEACHER_USERNAME = tp.USERNAME 
                    AND r.STATUS = 'PENDING') AS HAS_REQUEST 
             FROM TEACHER tp WHERE tp.ID = ?
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

    @Override
    public TeacherProfile getEntityFromResultSet (ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String username = resultSet.getString("USERNAME");
        String firstName = resultSet.getString("FIRST_NAME");
        String lastName = resultSet.getString("LAST_NAME");
        String email = resultSet.getString("EMAIL");
        LocalDate dateAdded = resultSet.getDate("DATE_ADDED").toLocalDate();
        boolean hasRequest = resultSet.getBoolean("HAS_REQUEST");

        TeacherProfile teacherProfile = new TeacherProfile(id, username, firstName, lastName,
                email, dateAdded);

        teacherProfile.setCoursesCount(resultSet.getInt("courses_count"));
        teacherProfile.setRequestedPasswordReset(hasRequest);

        return teacherProfile;
    }

    @Override
    public void saveAll(List<TeacherProfile> teachers) {

        String query = "INSERT INTO TEACHER (USERNAME, ROLE, FIRST_NAME, LAST_NAME, EMAIL, DATE_ADDED) VALUES (?, ?, ?, ?, ?, ?)";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (TeacherProfile teacher : teachers){
                preparedStatement.setString(1, teacher.getUsername());
                preparedStatement.setString(2,teacher.getRole().name());
                preparedStatement.setString(3, teacher.getFirstName());
                preparedStatement.setString(4, teacher.getLastName());
                preparedStatement.setString(5, teacher.getEmail());
                preparedStatement.setDate(6, Date.valueOf(teacher.getDateAdded()));
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void save (TeacherProfile teacher) {
        List<TeacherProfile> teachers = new ArrayList<>();
        teachers.add(teacher);
        saveAll(teachers);
    }

    @Override
    public void delete(String username) {

        String query = "DELETE FROM TEACHER WHERE USERNAME = ?";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();

        } catch (SQLException | IOException e){
            throw new RepositoryException(e);
        }
    }

    @Override
    public void update(TeacherProfile teacher, String oldUsername) {

        String query = """
                UPDATE TEACHER
                SET USERNAME = ?, FIRST_NAME = ?, LAST_NAME = ?, EMAIL = ?
                WHERE USERNAME = ?
                """;

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, teacher.getUsername());
            preparedStatement.setString(2, teacher.getFirstName());
            preparedStatement.setString(3, teacher.getLastName());
            preparedStatement.setString(4, teacher.getEmail());
            preparedStatement.setString(5, oldUsername);
            preparedStatement.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Integer count() {
        String query = "SELECT COUNT(*) FROM TEACHER";

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
