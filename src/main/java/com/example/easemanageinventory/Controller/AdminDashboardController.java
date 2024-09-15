package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.Database.DBSystem;
import com.example.easemanageinventory.Model.User;
import com.example.easemanageinventory.Model.UserModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class AdminDashboardController{

    @FXML private ComboBox userRole;
    @FXML private Label loggedUserName;
    @FXML private TableView<User> userListTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userStatusColumn;
    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private CheckBox showPassword;



    @FXML private void initialize(){
        loggedUserName.setText("ADMIN");
        ObservableList<String> roles = FXCollections.observableArrayList("Admin","Manager","Viewer");
        userRole.setItems(roles);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("userrole"));
        userStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadUserTable();

        userListTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // Update TextField when selection changes
                    if (newValue != null) {
                        username.setEditable(false);
                        username.setText(newValue.getUsername());
                        userRole.setValue(newValue.getUserrole());
                        password.setText(newValue.getPassword());
                    }
                }
        );
    }

    private void loadUserTable() {
        ObservableList<User> userList = DBSystem.listAllUsers();
        userListTable.setItems(userList);
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

    public void onAddUserClick(ActionEvent event) throws SQLException {
        UserModel userModel = new UserModel();
        userModel.setUsername(username.getText());
        userModel.setPassword(password.getText());
        userModel.setUserrole((String) userRole.getValue());
        if(DBSystem.insertNewUser(userModel)){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("USER CREATED SUCCESSFULLY!");
            alert.show();
        }

    }

    public void onUpdateUserClick(ActionEvent event) {
        UserModel userModel = new UserModel();
        userModel.setUsername(username.getText());
        userModel.setPassword(password.getText());
        userModel.setUserrole((String) userRole.getValue());
        if (DBSystem.updateUser(userModel)){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("USER UPDATED SUCCESSFULLY!");
            alert.show();
        } else {
            EaseManageSystemController.showErrorWindow("Error! User not updated!");
        }
    }

    public void onDeleteUserClick(ActionEvent event) throws SQLException {
        UserModel userModel = new UserModel();
        if (!Objects.equals(username.getText(), "admin")){
            userModel.setUsername(username.getText());
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Do you want to delete user?");
            ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
            if (result ==  ButtonType.OK){
                if (DBSystem.deleteUser(userModel)){
                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                    alert1.setContentText("USER DELETED SUCCESSFULLY!");
                    alert1.show();
                } else {
                    EaseManageSystemController.showErrorWindow("USER NOT DELETED!");
                }

            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("ADMIN CANNOT BE DELETED!");
            alert.show();
        }
    }

    public void onActivateUserClick(ActionEvent event) {
        UserModel userModel = new UserModel();
        userModel.setUsername(username.getText());
        if (DBSystem.activateUser(userModel)){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("USER ACTIVATED SUCCESSFULLY!");
            alert.show();
        } else {
            EaseManageSystemController.showErrorWindow("Error! User not activated!");
        }
    }

    public void onRefreshClick(ActionEvent event) {
        loadUserTable();
    }

    public void onClearDataClick(ActionEvent event) {
        username.setText(null);
        username.setEditable(true);
        password.setText(null);
        userRole.setValue(null);
    }
}
