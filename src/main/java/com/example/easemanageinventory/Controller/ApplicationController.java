package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.MainApplication;

public class ApplicationController {

    private MainApplication mainApp;

    public void setMainApp(MainApplication mainApplication) {
        this.mainApp = mainApplication;
    }


    public void showLoginPage(){
        mainApp.showLoginPage();
    }

    public void showRegisterPage(){
        mainApp.showRegisterPage();
    }

    public void exitApplication(){
        mainApp.exitApplication();
    }
}
