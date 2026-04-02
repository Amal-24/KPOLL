package com.kpollman.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDBConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://mysql-2276d50ckpolljava26.h.aivencloud.com:24584/kpollman2026?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=10000";
        String user = "avnadmin";
        String password = "YOUR_PASSWORD_HERE"; // User should replace this manually for testing

        System.out.println("Attempting to connect to: " + url);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("SUCCESS: Connected to database!");
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            if (rs.next()) {
                System.out.println("Query test (SELECT 1) successful.");
            }
            conn.close();
        } catch (Exception e) {
            System.err.println("FAILED: Connection error!");
            e.printStackTrace();
        }
    }
}
