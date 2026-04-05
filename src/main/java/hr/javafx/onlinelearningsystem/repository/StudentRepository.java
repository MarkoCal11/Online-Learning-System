package hr.javafx.onlinelearningsystem.repository;

import hr.javafx.onlinelearningsystem.exception.EntityNotFoundException;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;
import hr.javafx.onlinelearningsystem.model.StudentProfile;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static hr.javafx.onlinelearningsystem.util.DBConnection.connectToDatabase;

public class StudentRepository implements UserRepository<StudentProfile> {

    @Override
    public List<StudentProfile> findAll(){

        List<StudentProfile> students = new ArrayList<>();

        String query =  """
            SELECT sp.*, COUNT(se.COURSE_ID) as courses_count,
            EXISTS(SELECT 1
                   FROM PASSWORD_RESET_REQUESTS R 
                   WHERE R.STUDENT_USERNAME = SP.USERNAME
                   AND R.STATUS = 'PENDING') AS HAS_REQUEST
            FROM STUDENT sp
            LEFT JOIN ENROLMENT se ON sp.ID = se.STUDENT_ID
            GROUP BY sp.ID
        """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                StudentProfile student = getEntityFromResultSet(resultSet);
                students.add(student);
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
        return students;
    }

    @Override
    public StudentProfile findByUsername (String username) {

        String query = """
             SELECT tp.*,
             (SELECT COUNT(*) FROM ENROLMENT e WHERE e.STUDENT_ID = tp.ID) AS courses_count,
             EXISTS(SELECT 1
                  FROM PASSWORD_RESET_REQUESTS r
                  WHERE r.STUDENT_USERNAME = tp.USERNAME
                  AND r.STATUS = 'PENDING') AS HAS_REQUEST
            FROM STUDENT tp
            WHERE tp.USERNAME = ?
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
    public StudentProfile findById (Integer id) {

        String query = """
             SELECT tp.*,
             (SELECT COUNT(*) FROM ENROLMENT e WHERE e.STUDENT_ID = tp.ID) AS courses_count,
             EXISTS(SELECT 1 
                    FROM PASSWORD_RESET_REQUESTS r 
                    WHERE r.STUDENT_USERNAME = tp.USERNAME 
                    AND r.STATUS = 'PENDING') AS HAS_REQUEST 
             FROM STUDENT tp WHERE tp.ID = ?
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
    public StudentProfile getEntityFromResultSet (ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String username = resultSet.getString("USERNAME");
        String firstName = resultSet.getString("FIRST_NAME");
        String lastName = resultSet.getString("LAST_NAME");
        String email = resultSet.getString("EMAIL");
        LocalDate dateAdded = resultSet.getDate("DATE_ADDED").toLocalDate();
        String jmbag = resultSet.getString("JMBAG");
        boolean hasRequest = resultSet.getBoolean("HAS_REQUEST");

        StudentProfile studentProfile = new StudentProfile(id, username, firstName, lastName,
                email, dateAdded, jmbag);

        studentProfile.setCoursesCount(resultSet.getInt("courses_count"));
        studentProfile.setRequestedPasswordReset(hasRequest);

        return studentProfile;
    }

    @Override
    public void saveAll(List<StudentProfile> students) {

        String query = "INSERT INTO STUDENT (USERNAME, ROLE, FIRST_NAME, LAST_NAME, EMAIL, DATE_ADDED, JMBAG) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (StudentProfile student : students){
                preparedStatement.setString(1, student.getUsername());
                preparedStatement.setString(2,student.getRole().name());
                preparedStatement.setString(3, student.getFirstName());
                preparedStatement.setString(4, student.getLastName());
                preparedStatement.setString(5, student.getEmail());
                preparedStatement.setDate(6, Date.valueOf(student.getDateAdded()));
                preparedStatement.setString(7, student.getJmbag());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void save (StudentProfile student) {
        List<StudentProfile> students = new ArrayList<>();
        students.add(student);
        saveAll(students);
    }

    @Override
    public void delete(String username) {

        String query = "DELETE FROM STUDENT WHERE USERNAME = ?";

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();

        } catch (SQLException | IOException e){
            throw new RepositoryException(e);
        }
    }

    @Override
    public void update(StudentProfile student, String oldUsername) {

        String query = """
                UPDATE STUDENT
                SET USERNAME = ?, FIRST_NAME = ?, LAST_NAME = ?, EMAIL = ?, JMBAG = ?
                WHERE USERNAME = ?
                """;

        try(Connection connection = connectToDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, student.getUsername());
            preparedStatement.setString(2, student.getFirstName());
            preparedStatement.setString(3, student.getLastName());
            preparedStatement.setString(4, student.getEmail());
            preparedStatement.setString(5, student.getJmbag());
            preparedStatement.setString(6, oldUsername);
            preparedStatement.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Integer count() {
        String query = "SELECT COUNT(*) FROM STUDENT";

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