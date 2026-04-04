package com.kpollman.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseHelper {
    private static HikariDataSource dataSource;

    static {
        Properties properties = new Properties();
        try (InputStream is = DatabaseHelper.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new IOException("db.properties not found in classpath");
            }
            properties.load(is);
            String URL = properties.getProperty("db.url");
            String USER = properties.getProperty("db.user");
            String PASSWORD = properties.getProperty("db.password");
            
            if (URL == null || USER == null || PASSWORD == null) {
                throw new IOException("Incomplete configuration in db.properties");
            }

            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            // Pool configuration
            config.setMinimumIdle(2);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000); // 30 seconds
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            config.setLeakDetectionThreshold(60000); // 1 minute

            // Additional MySQL specific properties
            config.addDataSourceProperty("useSSL", "true");
            config.addDataSourceProperty("sslMode", "REQUIRED");
            config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
            config.addDataSourceProperty("serverTimezone", "UTC");
            config.addDataSourceProperty("connectTimeout", "15000");
            config.addDataSourceProperty("socketTimeout", "60000");

            dataSource = new HikariDataSource(config);

        } catch (IOException e) {
            System.err.println("DATABASE CONFIG ERROR: Could not load db.properties from classpath.");
            System.err.println("Expected location: src/main/resources/db.properties");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection pool not initialized. Check db.properties.");
        }
        return dataSource.getConnection();
    }

    // Method to close the pool when shutting down the application
    public static void closePool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
