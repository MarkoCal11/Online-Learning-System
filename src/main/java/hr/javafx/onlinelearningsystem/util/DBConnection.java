package hr.javafx.onlinelearningsystem.util;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final Path DATABASE_FILE= Path.of("database.properties");
    private static final Path DATABASE_EXAMPLE_FILE = Path.of("database.properties.example");

    public static Connection connectToDatabase() throws SQLException, IOException{

        Properties properties = new Properties();

        Path toLoad;
        if (Files.exists(DATABASE_FILE)) {
            toLoad = DATABASE_FILE;
        } else if (Files.exists(DATABASE_EXAMPLE_FILE)) {
            toLoad = DATABASE_EXAMPLE_FILE;
        } else {
            throw new IOException(
                    "Missing database config. Create '" + DATABASE_FILE + "' or provide '" + DATABASE_EXAMPLE_FILE + "'."
            );
        }

        try(FileReader fileReader = new FileReader(toLoad.toFile())){
            properties.load(fileReader);
        }

        String url = properties.getProperty("databaseUrl");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        if (url == null || url.isBlank()) {
            throw new IOException("Invalid DB config in " + toLoad + ": property 'databaseUrl' is missing/blank.");
        }

        if (username == null) username = "";
        if (password == null) password = "";

        return DriverManager.getConnection(url, username, password);
    }

    public void disconnectFromDatabase(Connection connection) throws SQLException{
        connection.close();
    }
}