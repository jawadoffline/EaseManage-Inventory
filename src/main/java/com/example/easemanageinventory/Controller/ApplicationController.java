package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.MainApplication;
import com.example.easemanageinventory.Model.UserModel;

import java.sql.SQLException;

public class ApplicationController {

    private MainApplication mainApp;

    public void setMainApp(MainApplication mainApplication) {
        this.mainApp = mainApplication;
    }

    public void registerNewUser(UserModel user) throws SQLException {
        EaseManageSystemController.registerUser(user);
    }
}
