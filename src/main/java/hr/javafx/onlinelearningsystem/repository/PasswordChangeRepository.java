package hr.javafx.onlinelearningsystem.repository;

import hr.javafx.onlinelearningsystem.enums.UserRole;
import hr.javafx.onlinelearningsystem.exception.RepositoryException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static hr.javafx.onlinelearningsystem.util.DBConnection.connectToDatabase;

public class PasswordChangeRepository {


    public void createRequest(String username, UserRole role) {

        if(hasPendingRequest(username, role)) return;

        String query = role == UserRole.STUDENT
                ? "INSERT INTO PASSWORD_RESET_REQUESTS (STUDENT_USERNAME, STATUS) VALUES (?, 'PENDING')"
                : "INSERT INTO PASSWORD_RESET_REQUESTS (TEACHER_USERNAME, STATUS) VALUES (?, 'PENDING')";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public boolean hasPendingRequest(String username, UserRole role) {

        String query = role == UserRole.STUDENT
                ? """
            SELECT COUNT(*) 
            FROM PASSWORD_RESET_REQUESTS
            WHERE STUDENT_USERNAME = ? AND STATUS = 'PENDING'
        """
                : """
            SELECT COUNT(*) 
            FROM PASSWORD_RESET_REQUESTS
            WHERE TEACHER_USERNAME = ? AND STATUS = 'PENDING'
        """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void markDone(String username, UserRole role) {

        String query = role == UserRole.STUDENT
                ? """
            UPDATE PASSWORD_RESET_REQUESTS
            SET STATUS = 'DONE'
            WHERE STUDENT_USERNAME = ? AND STATUS = 'PENDING'
            """
                : """
            UPDATE PASSWORD_RESET_REQUESTS
            SET STATUS = 'DONE'
            WHERE TEACHER_USERNAME = ? AND STATUS = 'PENDING'
            """;

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void deleteRequestsForUser(String username, UserRole role) {
        String query = role == UserRole.STUDENT
                ? "DELETE FROM PASSWORD_RESET_REQUESTS WHERE STUDENT_USERNAME = ?"
                : "DELETE FROM PASSWORD_RESET_REQUESTS WHERE TEACHER_USERNAME = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void updateUsernameInRequests(String oldUsername, String newUsername, UserRole role) {
        String query = role == UserRole.STUDENT
                ? "UPDATE PASSWORD_RESET_REQUESTS SET STUDENT_USERNAME = ? WHERE STUDENT_USERNAME = ?"
                : "UPDATE PASSWORD_RESET_REQUESTS SET TEACHER_USERNAME = ? WHERE TEACHER_USERNAME = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newUsername);
            preparedStatement.setString(2, oldUsername);
            preparedStatement.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryException(e);
        }
    }
}
