package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.Database.DBSystem;
import com.example.easemanageinventory.Database.User;
import com.example.easemanageinventory.Database.UserDAO;
import com.example.easemanageinventory.MainApplication;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
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
        if (DBSystem.authenticateUser(userName.getText(),password.getText())){
            User user = DBSystem.currentUser(userName.getText());
            if (Objects.equals(user.getStatus(), "Activated")){
                if (Objects.equals(user.getUserRole(), "Admin")){
                    loadAdminDashboard();
                }
            }
        } else {
            EaseManageSystemController.showErrorWindow("Invalid Credentials!");
        }


//        UserDAO userDAO = new UserDAO();
//        if (userDAO.authenticateUser(userName.getText(), password.getText())){
//            loginStatus.setText("Login Successful");
//        } else {
//            loginStatus.setText("Login not successful");
//        }


    }

    private void loadAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/easemanageinventory/adminDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            Stage stage = (Stage) userName.getScene().getWindow();
            stage.setHeight(720);
            stage.setWidth(1280);
            stage.centerOnScreen();
            stage.getScene().setRoot(dashboardRoot);
            stage.setTitle("Admin Dashboard");

        } catch (IOException e) {
            throw new RuntimeException(e);
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
        Platform.exit();
    }

    public void setAppController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }
}

