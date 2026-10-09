package com.banking.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class managing database connectivity via JDBC.
 * Supports hierarchical configuration resolution:
 * 1. Programmatic test overrides (for isolated unit/integration tests)
 * 2. Environment variables (DB_URL, DB_USER, DB_PASSWORD)
 * 3. System properties (db.url, db.user, db.password)
 * 4. Classpath properties file (db.properties)
 * 5. Default fallback connection settings
 */
public class DBConnection {

    private static final String DEFAULT_URL = 
            "jdbc:mysql://localhost:3306/banking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root@123";

    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        Properties properties = new Properties();

        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("Notice: db.properties could not be loaded from classpath: " + e.getMessage());
        }

        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPassword = System.getenv("DB_PASSWORD");

        String sysUrl = System.getProperty("db.url");
        String sysUser = System.getProperty("db.user");
        String sysPassword = System.getProperty("db.password");

        url = envUrl != null ? envUrl : (sysUrl != null ? sysUrl : properties.getProperty("db.url", DEFAULT_URL));
        user = envUser != null ? envUser : (sysUser != null ? sysUser : properties.getProperty("db.user", DEFAULT_USER));
        password = envPassword != null ? envPassword : (sysPassword != null ? sysPassword : properties.getProperty("db.password", DEFAULT_PASSWORD));
    }

    /**
     * Allows test suites to redirect DBConnection to an in-memory or alternate test database.
     */
    public static synchronized void setConfiguration(String testUrl, String testUser, String testPassword) {
        url = testUrl;
        user = testUser;
        password = testPassword;
    }

    /**
     * Resets configuration back to environment / file defaults.
     */
    public static synchronized void resetToDefault() {
        loadConfiguration();
    }

    /**
     * Obtains a new database connection.
     * Callers must close the connection using try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests connectivity to the configured database.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }
}
