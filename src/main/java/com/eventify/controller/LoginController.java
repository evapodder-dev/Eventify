package com.eventify.controller;

import com.eventify.dao.UserDAO;
import com.eventify.model.User;
import com.eventify.util.AppExecutor;
import com.eventify.util.SceneNavigator;
import com.eventify.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.util.Optional;

/**
 * Controller for login.fxml.
 * Uses a background JavaFX Task executed via AppExecutor so SQLite authentication
 * and Sign Up registration never block the JavaFX Application Thread.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Button exitButton;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        statusLabel.setText("");
        if (roleCombo != null) {
            roleCombo.setItems(FXCollections.observableArrayList("Participant", "Organizer"));
            roleCombo.setValue("Participant");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
            statusLabel.setText("Please enter both Roll / User ID and password.");
            return;
        }

        setLoadingState(true, "Signing in with SQLite...");

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
                statusLabel.setText("Welcome, " + user.getUsername() + "!");

                if (SceneNavigator.class.getResource("/fxml/dashboard.fxml") != null) {
                    SceneNavigator.navigateTo("dashboard.fxml", "Dashboard (" + user.getRole() + " - " + user.getUsername() + ")");
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sign In Successful");
                    alert.setHeaderText("Welcome, " + user.getUsername());
                    alert.setContentText("Signed in as " + user.getRole() + "\n" + user.getAccessSummary());
                    alert.showAndWait();
                }
            } else {
                statusLabel.setStyle("-fx-text-fill: #dc2626;");
                statusLabel.setText("Account not found or wrong password. Click 'Sign Up' to create this ID!");
            }
        });

        authTask.setOnFailed(event -> {
            setLoadingState(false, "");
            Throwable ex = authTask.getException();
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
            statusLabel.setText("Sign In error: " + (ex != null ? ex.getMessage() : "Unknown error"));
        });

        AppExecutor.getExecutor().submit(authTask);
    }

    @FXML
    private void handleSignUp() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";
        String selectedRole = (roleCombo != null && roleCombo.getValue() != null) ? roleCombo.getValue() : "Participant";

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
            statusLabel.setText("Enter your Roll / User ID (e.g., 2307032) and password to Sign Up.");
            return;
        }

        setLoadingState(true, "Creating account for " + username + "...");

        Task<User> signUpTask = new Task<>() {
            @Override
            protected User call() {
                return userDAO.registerUser(username, password, selectedRole);
            }
        };

        signUpTask.setOnSucceeded(event -> {
            setLoadingState(false, "");
            User user = signUpTask.getValue();
            SessionManager.setCurrentUser(user);
            statusLabel.setStyle("-fx-text-fill: #16a34a;");
            statusLabel.setText("Account created! Welcome, " + user.getUsername() + "!");

            if (SceneNavigator.class.getResource("/fxml/dashboard.fxml") != null) {
                SceneNavigator.navigateTo("dashboard.fxml", "Dashboard (" + user.getRole() + " - " + user.getUsername() + ")");
            }
        });

        signUpTask.setOnFailed(event -> {
            setLoadingState(false, "");
            Throwable ex = signUpTask.getException();
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
            statusLabel.setText(ex != null ? ex.getMessage() : "Sign Up failed.");
        });

        AppExecutor.getExecutor().submit(signUpTask);
    }

    @FXML
    private void handleFillOrganizer() {
        usernameField.setText("admin");
        passwordField.setText("admin123");
        if (roleCombo != null) {
            roleCombo.setValue("Organizer");
        }
        statusLabel.setStyle("-fx-text-fill: #0284c7;");
        statusLabel.setText("Organizer credentials filled. Click Sign In.");
    }

    @FXML
    private void handleFillParticipant() {
        usernameField.setText("2307032");
        passwordField.setText("student123");
        if (roleCombo != null) {
            roleCombo.setValue("Participant");
        }
        statusLabel.setStyle("-fx-text-fill: #0f766e;");
        statusLabel.setText("Participant Roll (2307032) filled. Click Sign In.");
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
        if (signUpButton != null) {
            signUpButton.setDisable(loading);
        }
        statusLabel.setStyle("-fx-text-fill: #475569;");
        statusLabel.setText(message);
    }
}
