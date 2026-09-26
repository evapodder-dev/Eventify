package com.eventify.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Utility class for navigating between FXML screens on the primary Stage.
 * Supports Back and Forward page history navigation using Java Deque stacks.
 */
public class SceneNavigator {

    public record PageEntry(String fxmlFileName, String title) {
    }

    private static Stage primaryStage;
    private static final Deque<PageEntry> BACK_STACK = new ArrayDeque<>();
    private static final Deque<PageEntry> FORWARD_STACK = new ArrayDeque<>();
    private static PageEntry currentPage;
    private static Integer contextEventId;

    private SceneNavigator() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setContextEventId(Integer eventId) {
        contextEventId = eventId;
    }

    public static Integer consumeContextEventId() {
        Integer id = contextEventId;
        contextEventId = null;
        return id;
    }

    public static void clearHistory() {
        BACK_STACK.clear();
        FORWARD_STACK.clear();
        currentPage = null;
        contextEventId = null;
    }

    public static void navigateTo(String fxmlFileName, String title) {
        if ("login.fxml".equalsIgnoreCase(fxmlFileName)) {
            clearHistory();
            loadSceneInternal(new PageEntry(fxmlFileName, title));
            return;
        }

        if (currentPage != null && !currentPage.fxmlFileName().equalsIgnoreCase(fxmlFileName)) {
            BACK_STACK.push(currentPage);
            FORWARD_STACK.clear();
        }
        loadSceneInternal(new PageEntry(fxmlFileName, title));
    }

    public static boolean canGoBack() {
        return !BACK_STACK.isEmpty();
    }

    public static boolean canGoForward() {
        return !FORWARD_STACK.isEmpty();
    }

    public static void goBack() {
        if (!canGoBack()) {
            return;
        }
        if (currentPage != null) {
            FORWARD_STACK.push(currentPage);
        }
        PageEntry previous = BACK_STACK.pop();
        loadSceneInternal(previous);
    }

    public static void goForward() {
        if (!canGoForward()) {
            return;
        }
        if (currentPage != null) {
            BACK_STACK.push(currentPage);
        }
        PageEntry next = FORWARD_STACK.pop();
        loadSceneInternal(next);
    }

    public static String getPreviousPageTitle() {
        return BACK_STACK.isEmpty() ? "" : BACK_STACK.peek().title();
    }

    public static String getNextPageTitle() {
        return FORWARD_STACK.isEmpty() ? "" : FORWARD_STACK.peek().title();
    }

    public static String getBreadcrumbTrail() {
        List<String> names = new ArrayList<>();
        var iterator = BACK_STACK.descendingIterator();
        while (iterator.hasNext()) {
            names.add(iterator.next().title());
        }
        if (currentPage != null) {
            names.add("[" + currentPage.title() + "]");
        }
        if (names.size() > 4) {
            names = names.subList(names.size() - 4, names.size());
        }
        return String.join("  >  ", names);
    }

    private static void loadSceneInternal(PageEntry target) {
        try {
            String path = "/fxml/" + target.fxmlFileName();
            URL resource = SceneNavigator.class.getResource(path);
            if (resource == null) {
                showError("Navigation Error", "FXML screen not found: " + path);
                return;
            }
            currentPage = target;
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            double width = (primaryStage.getScene() != null) ? primaryStage.getScene().getWidth() : 1180.0;
            double height = (primaryStage.getScene() != null) ? primaryStage.getScene().getHeight() : 720.0;
            if (width < 800) width = 1180.0;
            if (height < 550) height = 720.0;

            Scene scene = new Scene(root, width, height);
            primaryStage.setTitle("Eventify — " + target.title());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            showError("Screen Load Error", "Could not load " + target.fxmlFileName() + ": " + e.getMessage());
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
