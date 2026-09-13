package com.eventify.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class for navigating between FXML screens on the primary Stage.
 */
public class SceneNavigator {

    private static Stage primaryStage;

    private SceneNavigator() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void navigateTo(String fxmlFileName, String title) {
        try {
            String path = "/fxml/" + fxmlFileName;
            URL resource = SceneNavigator.class.getResource(path);
            if (resource == null) {
                showError("Navigation Error", "FXML screen not found: " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1180, 720);
            primaryStage.setTitle("Eventify — " + title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            showError("Screen Load Error", "Could not load " + fxmlFileName + ": " + e.getMessage());
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
