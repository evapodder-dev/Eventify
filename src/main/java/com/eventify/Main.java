package com.eventify;

import com.eventify.database.DatabaseInitializer;
import com.eventify.util.AppExecutor;
import com.eventify.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for Eventify — Event Management & Coordination System.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseInitializer.initialize();
        SceneNavigator.setPrimaryStage(primaryStage);
        SceneNavigator.navigateTo("login.fxml", "Login");
    }

    @Override
    public void stop() {
        AppExecutor.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
