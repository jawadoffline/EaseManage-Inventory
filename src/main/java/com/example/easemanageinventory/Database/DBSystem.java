package com.example.easemanageinventory.Database;

import com.example.easemanageinventory.Controller.EaseManageSystemController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBSystem {

    public static boolean insertNewUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e){
            if ("23505".equals(e.getSQLState())) {
                EaseManageSystemController.showErrorWindow("Username already exists!");
                System.out.println("Username already exists.");
            } else {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection !=  null) connection.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }


    public static boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if a matching record is found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User currentUser(String userName) {
        String query = "SELECT id, username, userrole, status FROM users WHERE username = ?";
        User user = null;
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, userName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String userRole = resultSet.getString("userrole");
                    String status = resultSet.getString("status");

                    user = new User(id, userName, userRole, status );

                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }
}
