package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.Database.DBSystem;
import com.example.easemanageinventory.Database.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;


public class EaseManageSystemController {

    public static void registerUser(User user) throws SQLException {
        boolean registrationStatus = DBSystem.insertNewUser(user);
        if (registrationStatus){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("User Created Successfully!");
            alert.show();
        }
    }

    public static void showErrorWindow(String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(s);
        alert.show();
    }

    public static void loadAdminDashboard(TextField userName) {
        try {
            FXMLLoader loader = new FXMLLoader(EaseManageSystemController.class.getResource("adminDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            Stage stage = (Stage) userName.getScene().getWindow();
            stage.getScene().setRoot(dashboardRoot);
            stage.setTitle("Admin Dashboard");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
