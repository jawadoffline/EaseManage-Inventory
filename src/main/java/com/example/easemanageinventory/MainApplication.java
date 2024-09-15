package com.example.easemanageinventory;

import com.example.easemanageinventory.Controller.ApplicationController;
import com.example.easemanageinventory.Controller.LoginController;
import com.example.easemanageinventory.Controller.RegisterController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApplication extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ApplicationController applicationController;
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("EaseManage Inventory - LOGIN");
        Image icon = new Image(getClass().getResource("/Images/logo.png").toExternalForm());
        this.primaryStage.getIcons().add(icon);

        initRootLayout();
        showLoginPage();
    }

    private void initRootLayout(){
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation((MainApplication.class.getResource("login.fxml")));
        try {
            rootLayout = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        applicationController = new ApplicationController();
        applicationController.setMainApp(this);

        FXMLLoader rootLoader = new FXMLLoader();
        rootLoader.setLocation(MainApplication.class.getResource("login.fxml"));
        try {
            rootLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.show();
    }

    public void showLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApplication.class.getResource("login.fxml"));
            BorderPane loginPage = loader.load();

            rootLayout.setCenter(loginPage);
            LoginController loginController = loader.getController();
            loginController.setAppController(applicationController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch();
    }

}

