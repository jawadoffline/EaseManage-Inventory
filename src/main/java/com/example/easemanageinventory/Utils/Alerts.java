package com.example.easemanageinventory.Utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Alerts {
    public static void showInformation(String string){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(string);
        alert.show();
    }

    public static void showError(String string){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(string);
        alert.show();
    }

    public static boolean getConfirmation(String string) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(string);
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        return result == ButtonType.OK;
    }
}
