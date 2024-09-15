package com.example.easemanageinventory.Model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {

    private final IntegerProperty userId;
    private final StringProperty username;
    private String password;
    private final StringProperty userRole;
    private final StringProperty status;

    public User(int userId, String username, String password, String userRole, String status) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.password = password;
        this.userRole = new SimpleStringProperty(userRole);
        this.status = new SimpleStringProperty(status);
    }


    public int getId() {
        return userId.get();
    }

    public IntegerProperty idProperty() {
        return userId;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getUserrole() {
        return userRole.get();
    }

    public void setUserRole(String userRole) {
        this.userRole.set(userRole);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
