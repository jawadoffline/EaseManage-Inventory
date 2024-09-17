package com.example.easemanageinventory.Controller;

import com.example.easemanageinventory.Database.DBSystem;
import com.example.easemanageinventory.Model.*;
import com.example.easemanageinventory.Utils.Alerts;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class AdminDashboardController{

    @FXML private ComboBox<String> userRole;
    @FXML private Label loggedUserName;
    @FXML private TableView<User> userListTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userStatusColumn;
    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private TableView<ItemsModel> itemsTable;
    @FXML private TableColumn<ItemsModel, Integer> itemCodeColumn;
    @FXML private TableColumn<ItemsModel, String> itemNameColumn;
    @FXML private TableColumn<ItemsModel, Integer> categoryColumn;
    @FXML private TableColumn<ItemsModel, Integer> availableStockColumn;
    @FXML private TableColumn<ItemsModel, Date> lastUpdateDateColumn;
    @FXML private TableColumn<ItemsModel, Integer> currentStatusColumn;
    @FXML private TableColumn<ItemsModel, Text> remarksColumn;


    //Add Categories
    @FXML private TextField enterCategory;
    @FXML private TableView<Categories> categoriesList;
    @FXML private TableColumn<Categories, Integer> categoryIdColumn;
    @FXML private TableColumn<Categories, String> allCategoryColumn;

    //Add Status
    @FXML private TextField enterStatus;
    @FXML private TableView<ItemStatus> statusList;
    @FXML private TableColumn<ItemStatus, Integer> statusIdColumn;
    @FXML private TableColumn<ItemStatus, String> allStatusColumn;

    //Add Item
    @FXML private TextField enterItemName;
    @FXML private ComboBox<String> selectCategoryAddItem;
    @FXML private TextField enterStock;
    @FXML private DatePicker enterDateAddItem;
    @FXML private ComboBox<String> selectStatusAddItem;
    @FXML private TextArea enterRemarksAddItem;
    @FXML private TableView<ItemsModel> allItemList;
    @FXML private TableColumn<ItemsModel, Integer> idItemColumn;
    @FXML private TableColumn<ItemsModel, String> allItemColumn;

    //Modify Item
    @FXML private ComboBox<String> selectItem;
    @FXML private TextField modifyStock;
    @FXML private DatePicker selectDateModifyItem;
    @FXML private ComboBox<String> selectCategoryModifyItem;
    @FXML private ComboBox<String> selectCurrentStatusModifyItem;
    @FXML private TextArea enterRemarksModifyItem;
    @FXML private TableView<ItemsModel> itemStockTable;
    @FXML private TableColumn<ItemsModel, String> itemModifyNameColumn;
    @FXML private TableColumn<ItemsModel, Integer> currentStockColumn;


    @FXML private void initialize() throws SQLException {
        loggedUserName.setText("ADMIN");
        ObservableList<String> roles = FXCollections.observableArrayList("Admin","Manager","Viewer");
        userRole.setItems(roles);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("userrole"));
        userStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadUserTable();

        //loading main dashboard
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        availableStockColumn.setCellValueFactory(new PropertyValueFactory<>("availableStock"));
        lastUpdateDateColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdateDate"));
        currentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        loadItemsData();


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

        //populate select categories combo box of Add Item
        List<String> categoriesAddItem = DBSystem.getCategories();
        selectCategoryAddItem.getItems().addAll(categoriesAddItem);


        //populate select status combo box of add item
        List<String> statusesAddItem = DBSystem.getStatuses();
        selectStatusAddItem.getItems().addAll(statusesAddItem);

        //populate select item of modify item
        List<String> selectModifyItem = DBSystem.getItems();
        selectItem.getItems().addAll(selectModifyItem);

        //populate select categories combo box of modify item
        List<String> categoriesModifyItem = DBSystem.getCategories();
        selectCategoryModifyItem.getItems().addAll(categoriesModifyItem);

        //populate select categories combo box of modify item
        List<String> statusesModifyItem = DBSystem.getStatuses();
        selectCurrentStatusModifyItem.getItems().addAll(statusesModifyItem);

    }

    private void loadItemsData() throws SQLException {
        ObservableList<ItemsModel> itemList = DBSystem.listItems();
        itemsTable.setItems(itemList);
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

    public void onAddUserClick(ActionEvent event) {
        UserModel userModel = new UserModel();
        userModel.setUsername(username.getText());
        userModel.setPassword(password.getText());
        userModel.setUserrole(userRole.getValue());
        if(DBSystem.insertNewUser(userModel)){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("USER CREATED SUCCESSFULLY!");
            alert.show();
        }
        loadUserTable();

    }

    public void onUpdateUserClick(ActionEvent event) {
        UserModel userModel = new UserModel();
        userModel.setUsername(username.getText());
        userModel.setPassword(password.getText());
        userModel.setUserrole(userRole.getValue());
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

    public void onClearSearchClick(ActionEvent event) {
    }

    public void onShowLowStockClick(ActionEvent event) {
    }

    public void onSearchClick(ActionEvent event) {
    }
    public void onAddCategoryClick(ActionEvent event) {
        String categoryText = enterCategory.getText().trim();
        if (categoryText.isEmpty()) {
            Alerts.showError("CATEGORY NOT ENTERED");
            return;
        }
        Categories category = new Categories();
        category.setCategory(categoryText);
        if (DBSystem.insertCategory(category)) {
            Alerts.showInformation("NEW CATEGORY ADDED!");
        } else {
            Alerts.showError("CATEGORY NOT ADDED!");
        }
        loadAllCategoryTable();
    }

    public void onDeleteCategoryClick(ActionEvent event) {
        String categoryText = enterCategory.getText().trim();
        if (categoryText.isEmpty()) {
            Alerts.showError("CATEGORY NOT ENTERED");
            return;
        }
        Categories category = new Categories();
        category.setCategory(categoryText);
        if (!Alerts.getConfirmation("Do you want to remove the category?")) {
            return;
        }
        if (DBSystem.deleteCategory(category)) {
            Alerts.showInformation("CATEGORY REMOVED");
        } else {
            Alerts.showError("CATEGORY NOT REMOVED. It may not exist.");
        }
        loadAllCategoryTable();
    }

    public void onViewAllCategories(ActionEvent event) {
        enterCategory.clear();
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        allCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        loadAllCategoryTable();
    }

    private void loadAllCategoryTable() {
        ObservableList<Categories> categoryList = DBSystem.listAllCategories();
        categoriesList.setItems(categoryList);
        categoriesList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // Update TextField when selection changes
                    if (newValue != null) {
                        enterCategory.setEditable(false);
                        enterCategory.setText(newValue.getCategory());
                    }
                }
        );
    }

    public void onViewAllStatus(ActionEvent event) {
        enterStatus.clear();
        statusIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        allStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadAllStatusTable();
    }

    private void loadAllStatusTable() {
        ObservableList<ItemStatus> itemStatusesList = DBSystem.listAllStatuses();
        statusList.setItems(itemStatusesList);
        statusList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // Update TextField when selection changes
                    if (newValue != null) {
                        enterStatus.setEditable(false);
                        enterStatus.setText(newValue.getStatus());
                    }
                }
        );
    }


    public void onAddStatusClick(ActionEvent event) {
        String statusText = enterStatus.getText().trim();
        if (statusText.isEmpty()) {
            Alerts.showError("STATUS NOT ENTERED");
            return;
        }
        ItemStatus itemStatus = new ItemStatus();
        itemStatus.setStatus(statusText);
        if (DBSystem.insertStatus(itemStatus)) {
            Alerts.showInformation("NEW ITEM STATUS ADDED!");
        } else {
            Alerts.showError("ITEM STATUS NOT ADDED!");
        }
        loadAllStatusTable();
    }

    public void onDeleteStatusClick(ActionEvent event) {
        String statusText = enterStatus.getText().trim();
        if (statusText.isEmpty()) {
            Alerts.showError("STATUS NOT ENTERED");
            return;
        }
        ItemStatus itemStatus = new ItemStatus();
        itemStatus.setStatus(statusText);
        if (!Alerts.getConfirmation("Do you want to remove the status?")) {
            return;
        }
        if (DBSystem.deleteStatus(itemStatus)) {
            Alerts.showInformation("ITEM STATUS REMOVED");
        } else {
            Alerts.showError("STATUS NOT REMOVED. It may not exist.");
        }
        loadAllStatusTable();
    }

    public void onAddItemClick(ActionEvent event) throws SQLException {
        String itemName = enterItemName.getText().trim();
        String category = selectCategoryAddItem.getValue();
        LocalDate date = enterDateAddItem.getValue();
        String status = selectStatusAddItem.getValue();
        String remarks = enterRemarksAddItem.getText();

        if (itemName.isEmpty()) {
            Alerts.showError("ITEM NAME CANNOT BE EMPTY");
            return;
        }

        if (category == null || category.isEmpty()) {
            Alerts.showError("CATEGORY MUST BE SELECTED");
            return;
        }

        int stock;
        try {
            stock = Integer.parseInt(enterStock.getText().trim());
            if (stock < 0) {
                Alerts.showError("STOCK CANNOT BE NEGATIVE");
                return;
            }
        } catch (NumberFormatException e) {
            Alerts.showError("INVALID STOCK VALUE");
            return;
        }

        if (date == null) {
            Alerts.showError("DATE MUST BE SELECTED");
            return;
        }

        if (status == null || status.isEmpty()) {
            Alerts.showError("STATUS MUST BE SELECTED");
            return;
        }

        int categoryId = DBSystem.getCategoryIdByName(category);
        int statusId = DBSystem.getStatusIdByName(status);
        ItemsModel itemsModel = new ItemsModel();
        itemsModel.setItemName(itemName);
        itemsModel.setCategoryId(categoryId);
        itemsModel.setAvailableStock(stock);
        itemsModel.setLastUpdateDate(date);
        itemsModel.setStatusId(statusId);
        itemsModel.setRemarks(remarks);

        if (DBSystem.insertItemRecord(itemsModel)){
            Alerts.showInformation("ITEM RECORD INSERTED!");
        } else {
            Alerts.showError("ITEM RECORD NOT INSERTED");
        }
        loadAllItems();
        loadItemsData();

    }

    public void onDeleteItemClick(ActionEvent event) throws SQLException {
        String enterItemNameText = enterItemName.getText().trim();
        if (enterItemNameText.isEmpty()) {
            Alerts.showError("ITEM NOT ENTERED");
            return;
        }
        ItemsModel itemsModel = new ItemsModel();
        itemsModel.setItemName(enterItemNameText);
        if (!Alerts.getConfirmation("Do you want to remove the item?")) {
            return;
        }
        if (DBSystem.deleteItemRecord(itemsModel)) {
            Alerts.showInformation("ITEM REMOVED");
        } else {
            Alerts.showError("ITEM NOT REMOVED. It may not exist.");
        }
        loadAllItems();
        loadItemsData();

    }

    public void onViewAllItemsList(ActionEvent event) throws SQLException {
        enterItemName.clear();
        enterStock.clear();
        enterDateAddItem.setValue(null);
        enterRemarksAddItem.clear();
        idItemColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        allItemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        loadAllItems();
    }

    private void loadAllItems() throws SQLException {
        ObservableList<ItemsModel> itemList = DBSystem.listItems();
        allItemList.setItems(itemList);
        allItemList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // Update TextField when selection changes
                    if (newValue != null) {
                        enterItemName.setEditable(false);
                        enterItemName.setText(newValue.getItemName());
                        selectCategoryAddItem.setEditable(false);
                        selectCategoryAddItem.setValue(newValue.getCategoryName());
                        enterStock.setEditable(false);
                        enterStock.setText(String.valueOf(newValue.getAvailableStock()));
                        enterDateAddItem.setEditable(false);
                        enterDateAddItem.setValue(newValue.getLastUpdateDate());
                        selectStatusAddItem.setEditable(false);
                        selectStatusAddItem.setValue(newValue.getStatus());
                        enterRemarksAddItem.setEditable(false);
                        enterRemarksAddItem.setText(newValue.getRemarks());
                    }
                }
        );
    }

    public void onUpdateItemClick(ActionEvent event) throws SQLException {
        String itemNameText = selectItem.getValue();
        String stockText = modifyStock.getText();
        int stock;
        try {
            stock = Integer.parseInt(stockText);
        } catch (NumberFormatException e) {
            Alerts.showError("PLEASE ENTER A VALID NUMBER!");
            return;
        }
        LocalDate date = selectDateModifyItem.getValue();
        String category = selectCategoryModifyItem.getValue();
        String status = selectCurrentStatusModifyItem.getValue();
        String remarks = enterRemarksModifyItem.getText();

        if (date == null) {
            Alerts.showError("DATE MUST BE SELECTED");
            return;
        }

        if (category == null || category.isEmpty()) {
            Alerts.showError("CATEGORY MUST BE SELECTED");
            return;
        }

        if (status == null || status.isEmpty()) {
            Alerts.showError("STATUS MUST BE SELECTED");
            return;
        }
        int categoryId = DBSystem.getCategoryIdByName(category);
        int statusId = DBSystem.getStatusIdByName(status);
        ItemsModel itemsModel = new ItemsModel();
        itemsModel.setItemName(itemNameText);
        itemsModel.setCategoryId(categoryId);
        itemsModel.setAvailableStock(stock);
        itemsModel.setLastUpdateDate(date);
        itemsModel.setStatusId(statusId);
        itemsModel.setRemarks(remarks);
        if (DBSystem.updateItemRecord(itemsModel)) {
            Alerts.showInformation("ITEM RECORD UPDATED!");
        } else {
            Alerts.showError("ITEM RECORD NOT UPDATED");
        }
        loadAllItems();
        loadItemsData();
        loadAllItemStock();
    }

    public void onViewAllItemsStockList(ActionEvent event) throws SQLException {
        itemModifyNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        currentStockColumn.setCellValueFactory(new PropertyValueFactory<>("availableStock"));
        loadAllItemStock();
    }

    private void loadAllItemStock() throws SQLException {
        ObservableList<ItemsModel> itemList = DBSystem.listItems();
        itemStockTable.setItems(itemList);
        itemStockTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // Update TextField when selection changes
                    if (newValue != null) {
                        selectItem.setValue(newValue.getItemName());
                        modifyStock.setText(String.valueOf(newValue.getAvailableStock()));
                        selectDateModifyItem.setValue(newValue.getLastUpdateDate());
                        selectCategoryModifyItem.setValue(newValue.getCategoryName());
                        selectCurrentStatusModifyItem.setValue(newValue.getStatus());
                        enterRemarksModifyItem.setText(newValue.getRemarks());
                    }
                }
        );    }

    public void selectItemSelected(ActionEvent event) throws SQLException {
        String itemNameText = selectItem.getSelectionModel().getSelectedItem();
        ItemsModel selectedItem = DBSystem.getItemByItemName(itemNameText);
        assert selectedItem != null;
        modifyStock.setText(String.valueOf(selectedItem.getAvailableStock()));
        selectDateModifyItem.setValue(selectedItem.getLastUpdateDate());
        selectCategoryModifyItem.setValue(selectedItem.getCategoryName());
        selectCurrentStatusModifyItem.setValue(selectedItem.getStatus());
        enterRemarksModifyItem.setText(selectedItem.getRemarks());
    }
}
