package com.hms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        // Load the Login screen
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/Login.fxml"));

        // Apply global stylesheet
        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm());

        // Window settings
        primaryStage.setTitle("Medicare Hospital — Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
