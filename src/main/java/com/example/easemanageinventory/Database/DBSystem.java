package com.example.easemanageinventory.Database;

import com.example.easemanageinventory.Controller.EaseManageSystemController;
import com.example.easemanageinventory.Model.*;
import com.example.easemanageinventory.Utils.Alerts;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
                "FROM allitems i\n" +
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
                itemsModel.setLastUpdateDate(lastUpdateDate.toLocalDate());
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

    public static boolean insertCategory(Categories category) {
        String string = "INSERT INTO categories (category) VALUES (?)";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(string);
            statement.setString(1, category.getCategory());
            statement.executeUpdate();
            return true;
        } catch (SQLException e){
            if ("23505".equals(e.getSQLState())) {
                Alerts.showError("CATEGORY ALREADY EXISTS!");
            } else {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection !=  null) connection.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteCategory(Categories category) {
        String query = "DELETE FROM categories where category = ?";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, category.getCategory());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e){
            if ("23503".equals(e.getSQLState())) {
                Alerts.showError("Cannot delete category. There are items with this category.");
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

    public static ObservableList<Categories> listAllCategories() {
        ObservableList<Categories> categoriesList = FXCollections.observableArrayList();
        String query = "SELECT * FROM categories";
        try(Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                String category = resultSet.getString("category");

                Categories categories = new Categories();
                categories.setId(id);
                categories.setCategory(category);
                categoriesList.add(categories);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return categoriesList;

    }

    public static boolean insertStatus(ItemStatus itemStatus) {
        String string = "INSERT INTO itemstatus (category) VALUES (?)";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(string);
            statement.setString(1, itemStatus.getStatus());
            statement.executeUpdate();
            return true;
        } catch (SQLException e){
            if ("23505".equals(e.getSQLState())) {
                Alerts.showError("STATUS ALREADY EXISTS!");
            } else {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection !=  null) connection.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteStatus(ItemStatus itemStatus) {
        String query = "DELETE FROM itemstatus where status = ?";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, itemStatus.getStatus());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e){
            if ("23503".equals(e.getSQLState())) {
                Alerts.showError("Cannot delete status. There are items with this status.");
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

    public static ObservableList<ItemStatus> listAllStatuses() {
        ObservableList<ItemStatus> statusList = FXCollections.observableArrayList();
        String query = "SELECT * FROM itemstatus";
        try(Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                String status = resultSet.getString("status");

                ItemStatus itemStatus = new ItemStatus();
                itemStatus.setId(id);
                itemStatus.setStatus(status);
                statusList.add(itemStatus);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statusList;

    }

    public static List<String> getCategories() {
        List<String> categoryList = new ArrayList<>();
        String query = "SELECT category FROM categories";
        try(Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()){
                String category = resultSet.getString("category");
                categoryList.add(category);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return categoryList;

    }

    public static List<String> getStatuses() {
        List<String> statusList = new ArrayList<>();
        String query = "SELECT status FROM itemstatus";
        try(Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()){
                String status = resultSet.getString("status");
                statusList.add(status);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statusList;
    }

    public static boolean insertItemRecord(ItemsModel itemsModel) {
        String string = "INSERT INTO allitems (itemname, availablestock, lastupdatedate, remarks, categoryid, statusid) VALUES (?,?,?,?,?,?)";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(string);
            statement.setString(1, itemsModel.getItemName());
            statement.setInt(2, itemsModel.getAvailableStock());
            statement.setDate(3, Date.valueOf(itemsModel.getLastUpdateDate()));
            statement.setString(4, itemsModel.getRemarks());
            statement.setInt(5, itemsModel.getCategoryId());
            statement.setInt(6, itemsModel.getStatusId());
            statement.executeUpdate();
            return true;
        } catch (SQLException e){
            if ("23505".equals(e.getSQLState())) {
                Alerts.showError("ITEM ALREADY EXISTS!");
            } else {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection !=  null) connection.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public static int getCategoryIdByName(String category) throws SQLException {
        String query = "SELECT id from categories WHERE category = ?";
        Connection connection = DBConnection.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, category);
        ResultSet resultSet = preparedStatement.executeQuery();
        int categoryId = -1;
        if (resultSet.next()){
            categoryId = resultSet.getInt("id");
        }

        return categoryId;
    }

    public static int getStatusIdByName(String status) throws SQLException {
        String query = "SELECT id from itemstatus WHERE status = ?";
        Connection connection = DBConnection.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, status);
        ResultSet resultSet = preparedStatement.executeQuery();
        int statusId = -1;
        if (resultSet.next()){
            statusId = resultSet.getInt("id");
        }
        return statusId;
    }

    public static boolean deleteItemRecord(ItemsModel itemsModel) {
        String query = "DELETE FROM allitems where itemname = ?";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, itemsModel.getItemName());
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

    public static List<String> getItems() {
        List<String> itemList = new ArrayList<>();
        String query = "SELECT itemname FROM allitems";
        try(Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()){
                String category = resultSet.getString("itemname");
                itemList.add(category);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return itemList;
    }

    public static ItemsModel getItemByItemName(String itemNameText) throws SQLException {
        String sql = "SELECT i.availablestock, i.lastupdatedate, i.remarks, c.category, s.status " +
                "FROM allitems i " +
                "JOIN categories c ON i.categoryid = c.id " +
                "JOIN itemstatus s ON i.statusid = s.id " +
                "WHERE i.itemname = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, itemNameText);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    ItemsModel itemsModel = new ItemsModel();
                    int availableStock = resultSet.getInt("availablestock");
                    Date lastUpdateDate = resultSet.getDate("lastupdatedate");
                    String remarks = resultSet.getString("remarks");
                    String category = resultSet.getString("category");
                    String status = resultSet.getString("status");

                    itemsModel.setAvailableStock(availableStock);
                    itemsModel.setLastUpdateDate(lastUpdateDate.toLocalDate());
                    itemsModel.setRemarks(remarks);
                    itemsModel.setCategoryName(category);
                    itemsModel.setStatus(status);

                    return itemsModel;
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            // Log the SQLException and rethrow if necessary
            e.printStackTrace();
            throw e;
        }
    }


    public static boolean updateItemRecord(ItemsModel itemsModel) {
        String string = "UPDATE allitems SET availablestock = ?, lastupdatedate = ?, categoryid = ?, statusid = ?, remarks = ? WHERE itemname = ?";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(string);
            statement.setInt(1, itemsModel.getAvailableStock());
            statement.setDate(2, Date.valueOf(itemsModel.getLastUpdateDate()));
            statement.setInt(3, itemsModel.getCategoryId());
            statement.setInt(4, itemsModel.getStatusId());
            statement.setString(5,itemsModel.getRemarks());
            statement.setString(6,itemsModel.getItemName());
            statement.executeUpdate();
            return true;
        } catch (SQLException e){
                e.printStackTrace();
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection !=  null) connection.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }
}
