package com.kpollman.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {
    private static final String URL ="jdbc:mysql://mysql-2276d50ckpolljava26.h.aivencloud.com:24584/kpollman2026useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=10000";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
}
