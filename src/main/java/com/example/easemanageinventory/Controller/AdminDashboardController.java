package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.Database.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminDashboardController{

    @FXML private ComboBox userRole;
    @FXML private Label loggedUserName;
    @FXML private TableView<User> userListTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userStatusColumn;



    @FXML private void initialize(){
        loggedUserName.setText("ADMIN");
        ObservableList<String> roles = FXCollections.observableArrayList("Admin","Manager","Viewer");
        userRole.setItems(roles);

    }

    public void onLogoutClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/easemanageinventory/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) userListTable.getScene().getWindow();
            stage.setHeight(400);
            stage.setWidth(600);
            stage.centerOnScreen();
            stage.getScene().setRoot(loginRoot);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onUserRoleAction(ActionEvent event) {

    }

    public void onAddUserClick(ActionEvent event) {
    }

    public void onUpdateUserClick(ActionEvent event) {
    }

    public void onDeleteUserClick(ActionEvent event) {
    }

    public void onActivateUserClick(ActionEvent event) {
    }

    public void onLockUserClick(ActionEvent event) {
    }

    public void onUnlockUserClick(ActionEvent event) {
    }
}
