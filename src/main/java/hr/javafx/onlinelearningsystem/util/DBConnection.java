package hr.javafx.onlinelearningsystem.util;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String DATABASE_FILE="database.properties";

    public static Connection connectToDatabase() throws SQLException, IOException{

        try(FileReader fileReader = new FileReader(DATABASE_FILE)){

            Properties properties = new Properties();

            properties.load(fileReader);

            String url = properties.getProperty("databaseUrl");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");

            return DriverManager.getConnection(url, username, password);
        }
    }

    public void disconnectFromDatabase(Connection connection) throws SQLException{
        connection.close();
    }
}