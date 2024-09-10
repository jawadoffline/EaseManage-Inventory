package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.Database.DBConnection;
import com.example.easemanageinventory.Database.UserDAO;
import com.example.easemanageinventory.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Login {
    @FXML private TextField userName;
    @FXML private PasswordField password;
    @FXML private Button loginSwitch;
    @FXML private Button registerSwitch;
    @FXML private Button exitButton;
    @FXML private Label loginStatus;
    @FXML private Button loginButton;


    private ApplicationController applicationController;
    private MainApplication mainApplication;


    @FXML
    private void onLoginButtonClick(ActionEvent event){
        UserDAO userDAO = new UserDAO();
        if (userDAO.authenticateUser(userName.getText(), password.getText())){
            loginStatus.setText("Login Successful");
        } else {
            loginStatus.setText("Login not successful");
        }


    }
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

