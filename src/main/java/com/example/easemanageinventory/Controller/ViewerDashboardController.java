package com.example.easemanageinventory.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewerDashboardController {
    @FXML private Label loggedUserName;

    @FXML private void initialize(){
        loggedUserName.setText("VIEWER");
    }

    public void onLogoutClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/easemanageinventory/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) loggedUserName.getScene().getWindow();
            stage.setHeight(400);
            stage.setWidth(600);
            stage.centerOnScreen();
            stage.getScene().setRoot(loginRoot);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
