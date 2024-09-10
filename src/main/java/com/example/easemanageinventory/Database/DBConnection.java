package com.example.easemanageinventory.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/EaseManageInventory";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    static {
        try {
            // Load PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("PostgreSQL JDBC driver not found.", e);
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
//
//    public static void closeConnection() {
//        if (connection != null) {
//            try {
//                connection.close();
//                System.out.println("Database connection closed.");
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
