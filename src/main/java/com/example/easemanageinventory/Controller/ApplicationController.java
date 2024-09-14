package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.Database.User;
import com.example.easemanageinventory.MainApplication;

import java.sql.SQLException;

public class ApplicationController {

    private MainApplication mainApp;

    public void setMainApp(MainApplication mainApplication) {
        this.mainApp = mainApplication;
    }

    public void registerNewUser(User user) throws SQLException {
        EaseManageSystemController.registerUser(user);
    }

    public void showLoginPage(){
        mainApp.showLoginPage();
    }

    public void showRegisterPage(){
        mainApp.showRegisterPage();
    }

}
