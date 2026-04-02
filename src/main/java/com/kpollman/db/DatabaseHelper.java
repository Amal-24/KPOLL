package com.kpollman.db;

import java.io.IOException;
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
                throw new IOException("db.properties not found in classpath");
            }
            properties.load(is);
            URL = properties.getProperty("db.url");
            USER = properties.getProperty("db.user");
            PASSWORD = properties.getProperty("db.password");
            
            if (URL == null || USER == null || PASSWORD == null) {
                throw new IOException("Incomplete configuration in db.properties");
            }
        } catch (IOException e) {
            System.err.println("DATABASE CONFIG ERROR: Could not load db.properties from classpath.");
            System.err.println("Expected location: src/main/resources/db.properties");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("Database settings not loaded. Check db.properties.");
        }

        String baseJdbcUrl = URL;
        if (baseJdbcUrl.contains("?")) {
            baseJdbcUrl = baseJdbcUrl.substring(0, baseJdbcUrl.indexOf("?"));
        }

        // Simplified, modern connection string for Aiven MySQL
        String fullUrl = baseJdbcUrl + "?useSSL=true"
                + "&sslMode=REQUIRED" // Modern way to enforce SSL
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC"
                + "&connectTimeout=15000" // 15 seconds
                + "&socketTimeout=60000"; // 60 seconds for data operations

        int maxRetries = 3;
        SQLException lastException = null;

        for (int i = 1; i <= maxRetries; i++) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(fullUrl, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL Driver not found.", e);
            } catch (SQLException e) {
                lastException = e;
                System.err.println("Connection attempt " + i + " failed: " + e.getMessage());
                if (i < maxRetries) {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                }
            }
        }

        System.err.println("CRITICAL: Failed to connect to Aiven MySQL after " + maxRetries + " attempts.");
        System.err.println("Verify: 1. Is your IP whitelisted in Aiven console? 2. Is the service 'Running'?");
        throw lastException;
    }
}
