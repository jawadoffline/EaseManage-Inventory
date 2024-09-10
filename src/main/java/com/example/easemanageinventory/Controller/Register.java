package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class Register {


    private ApplicationController appController;

    @FXML private Button loginSwitch;
    @FXML private Button registerSwitch;
    @FXML private Button backButton;

    private ApplicationController applicationController;
    private MainApplication mainApplication;

    @FXML
    private void onLoginSwitchButtonClick(ActionEvent event){
        applicationController.showLoginPage();
    }

    @FXML
    private void onRegisterSwitchButtonClick(ActionEvent event){
        applicationController.showRegisterPage();
    }

    @FXML
    private void onBackButtonClick(ActionEvent event){
        applicationController.showLoginPage();
    }

    public void setAppController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }
}
