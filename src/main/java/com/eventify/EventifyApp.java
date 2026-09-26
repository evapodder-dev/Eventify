package com.eventify;

import com.eventify.database.DatabaseInitializer;
import com.eventify.util.AppExecutor;
import com.eventify.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX Application lifecycle class for Eventify.
 */
public class EventifyApp extends Application {

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
}
