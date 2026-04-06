package com.kpollman.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseHelper {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        Properties properties = new Properties();
        try (InputStream is = DatabaseHelper.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                // Try relative path if resource as stream fails in some environments
                URL = "jdbc:mysql://localhost:3306/kpollman2026?useSSL=false&allowPublicKeyRetrieval=true";
                USER = "root";
                PASSWORD = "password";
            } else {
                properties.load(is);
                URL = properties.getProperty("db.url");
                USER = properties.getProperty("db.user");
                PASSWORD = properties.getProperty("db.password");
            }
            
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.err.println("DATABASE CONFIG ERROR: " + e.getMessage());
            // Fallback for demo
            URL = "jdbc:mysql://localhost:3306/kpollman2026?useSSL=false&allowPublicKeyRetrieval=true";
            USER = "root";
            PASSWORD = "password";
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("DATABASE CONNECTION FAILURE: Falling back to mock data mode. " + e.getMessage());
            throw e; // Still throw to allow individual screens to catch and show mock data
        }
    }

    // Method to close the pool when shutting down the application
    public static void closePool() {
        // No pool to close in basic implementation
    }
}
