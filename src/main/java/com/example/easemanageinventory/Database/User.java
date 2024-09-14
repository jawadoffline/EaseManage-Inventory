package com.example.easemanageinventory.Database;

public class User {

    private int userId;
    private String username;
    private String password;
    private String userRole;
    private String status;

    public User(int userId, String username, String userRole, String status) {
        this.userId = userId;
        this.username = username;
        this.userRole = userRole;
        this.status = status;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
