package com.example.easemanageinventory.Database;

import com.example.easemanageinventory.Controller.EaseManageSystemController;
import com.example.easemanageinventory.Model.ItemsModel;
import com.example.easemanageinventory.Model.User;
import com.example.easemanageinventory.Model.UserModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;

import java.sql.*;

public class DBSystem {

    public static boolean registerNewUser(UserModel user) throws SQLException {
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

    public static UserModel currentUser(String userName) {
        String query = "SELECT id, username, password, userrole, status FROM users WHERE username = ?";
        UserModel user = null;
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, userName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String password = resultSet.getString("password");
                    String userRole = resultSet.getString("userrole");
                    String status = resultSet.getString("status");
                    user = new UserModel();
                    user.setUserId(id);
                    user.setUsername(userName);
                    user.setPassword(password);
                    user.setUserrole(userRole);
                    user.setStatus(status);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public static ObservableList<User> listAllUsers() {
        ObservableList<User> usersList = FXCollections.observableArrayList();
        String query = "SELECT id, username, password, userrole, status FROM users";
        try(Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String userrole = resultSet.getString("userrole");
                String status = resultSet.getString("status");

                User user = new User(id, username, password, userrole, status);
                usersList.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return usersList;
    }

    public static boolean insertNewUser(UserModel user) {
        String sql = "INSERT INTO users (username, password, userrole) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getUserrole());
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

    public static boolean updateUser(UserModel userModel) {
        String query = "UPDATE users SET password = ?, userrole = ? WHERE username = ?";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userModel.getPassword());
            preparedStatement.setString(2, userModel.getUserrole());
            preparedStatement.setString(3, userModel.getUsername());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
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

    public static boolean activateUser(UserModel userModel) {
        String query = "UPDATE users SET status = ? WHERE username = ?";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "Activated");
            preparedStatement.setString(2, userModel.getUsername());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
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

    public static boolean deleteUser(UserModel userModel) throws SQLException {
        String query = "DELETE FROM users where username = ?";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userModel.getUsername());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
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

    public static ObservableList<ItemsModel> listItems() throws SQLException {
        ObservableList<ItemsModel> itemsList = FXCollections.observableArrayList();
        ItemsModel itemsModel;
        String query = "SELECT i.itemid, i.itemname, i.availablestock, i.lastupdatedate, i.remarks, c.category, s.status\n" +
                "FROM items i\n" +
                "JOIN categories c ON i.categoryid = c.id\n" +
                "JOIN itemstatus s ON i.statusid = s.id;";
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        try{
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                int id = resultSet.getInt("itemid");
                String itemname = resultSet.getString("itemname");
                int availableStock = resultSet.getInt("availablestock");
                Date lastUpdateDate = resultSet.getDate("lastupdatedate");
                String remarks = resultSet.getString("remarks");
                String category = resultSet.getString("category");
                String status = resultSet.getString("status");

                itemsModel = new ItemsModel();
                itemsModel.setItemId(id);
                itemsModel.setItemName(itemname);
                itemsModel.setAvailableStock(availableStock);
                itemsModel.setLastUpdateDate(lastUpdateDate);
                itemsModel.setRemarks(remarks);
                itemsModel.setCategoryName(category);
                itemsModel.setStatus(status);
                itemsList.add(itemsModel);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return itemsList;
    }
}
