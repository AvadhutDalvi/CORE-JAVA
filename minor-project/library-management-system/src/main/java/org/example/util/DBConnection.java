package org.example.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class providing managed MySQL database connections via JDBC.
 * Reads configuration hierarchically:
 * 1. Environment variables (DB_URL, DB_USER, DB_PASSWORD) or System properties
 * 2. Classpath resource (db.properties)
 * 3. Default fallback values for local development
 */
public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root@123";

    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties properties = new Properties();

        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not read db.properties, falling back to defaults: " + e.getMessage());
        }

        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPassword = System.getenv("DB_PASSWORD");

        url = envUrl != null ? envUrl : properties.getProperty("db.url", DEFAULT_URL);
        user = envUser != null ? envUser : properties.getProperty("db.user", DEFAULT_USER);
        password = envPassword != null ? envPassword : properties.getProperty("db.password", DEFAULT_PASSWORD);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
