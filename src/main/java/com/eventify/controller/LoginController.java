package com.eventify.controller;

import com.eventify.dao.UserDAO;
import com.eventify.model.User;
import com.eventify.util.AppExecutor;
import com.eventify.util.SceneNavigator;
import com.eventify.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.util.Optional;

/**
 * Controller for login.fxml.
 * Uses a background JavaFX Task executed via AppExecutor so SQLite authentication
 * never blocks the JavaFX Application Thread.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Button loginButton;

    @FXML
    private Button exitButton;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        statusLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        setLoadingState(true, "Verifying credentials with SQLite...");

        Task<Optional<User>> authTask = new Task<>() {
            @Override
            protected Optional<User> call() {
                return userDAO.authenticate(username, password);
            }
        };

        authTask.setOnSucceeded(event -> {
            setLoadingState(false, "");
            Optional<User> userOpt = authTask.getValue();
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                SessionManager.setCurrentUser(user);
                statusLabel.setStyle("-fx-text-fill: #16a34a;");
                statusLabel.setText("Welcome, " + user.getFullName() + "!");

                if (SceneNavigator.class.getResource("/fxml/dashboard.fxml") != null) {
                    SceneNavigator.navigateTo("dashboard.fxml", "Dashboard (" + user.getRole() + ")");
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Login Successful");
                    alert.setHeaderText("Authenticated as " + user.getRole());
                    alert.setContentText(user.getFullName() + "\n" + user.getAccessSummary());
                    alert.showAndWait();
                }
            } else {
                statusLabel.setStyle("-fx-text-fill: #dc2626;");
                statusLabel.setText("Invalid username or password. Try admin / admin123.");
            }
        });

        authTask.setOnFailed(event -> {
            setLoadingState(false, "");
            Throwable ex = authTask.getException();
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
            statusLabel.setText("Authentication error: " + (ex != null ? ex.getMessage() : "Unknown error"));
        });

        AppExecutor.getExecutor().submit(authTask);
    }

    @FXML
    private void handleFillOrganizer() {
        usernameField.setText("admin");
        passwordField.setText("admin123");
        statusLabel.setStyle("-fx-text-fill: #0284c7;");
        statusLabel.setText("Organizer credentials filled. Click Login.");
    }

    @FXML
    private void handleFillParticipant() {
        usernameField.setText("student");
        passwordField.setText("student123");
        statusLabel.setStyle("-fx-text-fill: #0f766e;");
        statusLabel.setText("Participant credentials filled. Click Login.");
    }

    @FXML
    private void handleExit() {
        AppExecutor.shutdown();
        Platform.exit();
    }

    private void setLoadingState(boolean loading, String message) {
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
        loginButton.setDisable(loading);
        statusLabel.setStyle("-fx-text-fill: #475569;");
        statusLabel.setText(message);
    }
}
