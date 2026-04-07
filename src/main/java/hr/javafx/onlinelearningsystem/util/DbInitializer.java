package hr.javafx.onlinelearningsystem.util;

import org.h2.tools.RunScript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DbInitializer {

    private DbInitializer() {}

    public static void initIfNeeded() {
        try (Connection connection = DBConnection.connectToDatabase()) {
            if (isInitialized(connection)) {
                return;
            }

            runSqlResource(connection, "/schema.sql");
            runSqlResource(connection, "/seed.sql");

        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed. Check schema.sql/seed.sql.", e);
        }
    }

    private static boolean isInitialized(Connection connection) throws SQLException {
        String sql = """
                SELECT COUNT(*) 
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'STUDENT'
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private static void runSqlResource(Connection connection, String resourcePath) throws SQLException {
        InputStream in = DbInitializer.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalStateException("Missing resource on classpath: " + resourcePath);
        }
        RunScript.execute(connection, new InputStreamReader(in, StandardCharsets.UTF_8));
    }
}
