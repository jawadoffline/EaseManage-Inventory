package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.Database.User;
import com.example.easemanageinventory.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.Objects;

public class RegisterController {


    private ApplicationController appController;

    @FXML private Button loginSwitch;
    @FXML private Button registerSwitch;
    @FXML private Button backButton;
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private Label errorMessage;

    private ApplicationController applicationController;
    private MainApplication mainApplication;
    private EaseManageSystemController easeManageSystemController;
    private User user;


    @FXML
    private void onRegisterUserButtonClick(ActionEvent event) throws SQLException {
        errorMessage.setText(null);
        user.setUsername(username.getText());
        if (Objects.equals(password.getText(), confirmPassword.getText())){
            user.setPassword(password.getText());
            applicationController.registerNewUser(user);
        } else {
            errorMessage.setText("Passwords do not match!");
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

    @FXML
    private void onBackButtonClick(ActionEvent event){
        applicationController.showLoginPage();
    }

    public void setAppController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }
}
