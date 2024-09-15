package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.Database.DBSystem;
import com.example.easemanageinventory.Model.UserModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
    @FXML private TextField userName;
    @FXML private PasswordField password;

    @FXML
    private void onLoginButtonClick(ActionEvent event){
        if (DBSystem.authenticateUser(userName.getText(),password.getText())){
            UserModel user = DBSystem.currentUser(userName.getText());
            if (Objects.equals(user.getStatus(), "Activated")){
                if (Objects.equals(user.getUserrole(), "Admin")){
                    loadAdminDashboard();
                } else if (Objects.equals(user.getUserrole(),"Manager")){
                    loadManagerDashboard();
                } else {
                    loadViewerDashboard();
                }
            } else if (Objects.equals(user.getStatus(), "Deactivated")){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("ACCOUNT NOT ACTIVATED");
                alert.setContentText("Please activate your account through Admin!");
                alert.show();
            }
        } else {
            EaseManageSystemController.showErrorWindow("Invalid Credentials!");
        }
    }

    private void loadViewerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/easemanageinventory/viewerDashboard.fxml"));
            Parent dashboardManager = loader.load();

            Stage stage = (Stage) userName.getScene().getWindow();
            stage.setHeight(720);
            stage.setWidth(1280);
            stage.centerOnScreen();
            stage.getScene().setRoot(dashboardManager);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadManagerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/easemanageinventory/managerDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            Stage stage = (Stage) userName.getScene().getWindow();
            stage.setHeight(720);
            stage.setWidth(1280);
            stage.centerOnScreen();
            stage.getScene().setRoot(dashboardRoot);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onLoginSwitchButtonClick(ActionEvent event){
        showLoginPage();
    }

    private void showLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/easemanageinventory/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) userName.getScene().getWindow();
            stage.setHeight(400);
            stage.setWidth(600);
            stage.centerOnScreen();
            stage.getScene().setRoot(loginRoot);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onRegisterSwitchButtonClick(ActionEvent event){
        showRegisterUserPage();
    }

    private void showRegisterUserPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/easemanageinventory/register.fxml"));
            Parent registerRoot = loader.load();

            Stage stage = (Stage) userName.getScene().getWindow();
            stage.setHeight(400);
            stage.setWidth(600);
            stage.centerOnScreen();
            stage.getScene().setRoot(registerRoot);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML void onExitButtonClick(ActionEvent event){
        Platform.exit();
    }

    public void setAppController(ApplicationController applicationController) {
    }
}

