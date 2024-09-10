package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Login {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button loginSwitch;
    @FXML private Button registerSwitch;
    @FXML private Button exitButton;

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

    @FXML void onExitButtonClick(ActionEvent event){
        applicationController.exitApplication();
    }

    public void setAppController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }
}

