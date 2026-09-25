package com.eventify.controller;

import com.eventify.model.ApiEvent;
import com.eventify.model.Event;
import com.eventify.service.ApiEventService;
import com.eventify.service.ApiEventService.ApiFetchResult;
import com.eventify.service.EventService;
import com.eventify.util.AppExecutor;
import com.eventify.util.SceneNavigator;
import com.eventify.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller for api-events.fxml.
 * Runs HTTP requests on a background thread via AppExecutor and updates the JavaFX UI safely.
 */
public class ApiEventController {

    @FXML
    private TextField apiUrlField;
    @FXML
    private Button fetchButton;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private TextArea rawJsonArea;

    @FXML
    private TableView<ApiEvent> apiEventsTable;
    @FXML
    private TableColumn<ApiEvent, Number> colId;
    @FXML
    private TableColumn<ApiEvent, String> colTitle;
    @FXML
    private TableColumn<ApiEvent, String> colCategory;
    @FXML
    private TableColumn<ApiEvent, String> colDate;
    @FXML
    private TableColumn<ApiEvent, String> colVenue;
    @FXML
    private TableColumn<ApiEvent, String> colOrganizer;
    @FXML
    private TableColumn<ApiEvent, Number> colMax;

    private final ApiEventService apiEventService = new ApiEventService();
    private final EventService eventService = new EventService();

    @FXML
    public void initialize() {
        apiUrlField.setText(apiEventService.getDefaultApiUrl());

        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate()));
        colVenue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVenue()));
        colOrganizer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrganizer()));
        colMax.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMaxParticipants()));

        handleLoadFallback();
    }

    @FXML
    private void handleFetchFromApi() {
        String url = apiUrlField.getText();
        loadingIndicator.setVisible(true);
        fetchButton.setDisable(true);
        statusLabel.setStyle("-fx-text-fill: #0284c7;");
        statusLabel.setText("Sending asynchronous HTTP GET request on background worker thread...");

        Task<ApiFetchResult> apiTask = new Task<>() {
            @Override
            protected ApiFetchResult call() {
                return apiEventService.fetchEventsFromApi(url);
            }
        };

        apiTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            fetchButton.setDisable(false);
            ApiFetchResult result = apiTask.getValue();
            if (result.success()) {
                statusLabel.setStyle("-fx-text-fill: #16a34a;");
                statusLabel.setText(result.message());
                apiEventsTable.setItems(FXCollections.observableArrayList(result.events()));
                rawJsonArea.setText(result.rawJsonPreview());
            } else {
                statusLabel.setStyle("-fx-text-fill: #dc2626;");
                statusLabel.setText(result.message());
                rawJsonArea.setText(result.rawJsonPreview());
                showAlert(Alert.AlertType.WARNING, "API Response Notice", result.message());
            }
        });

        apiTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            fetchButton.setDisable(false);
            Throwable ex = apiTask.getException();
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
            statusLabel.setText("Error: " + (ex != null ? ex.getMessage() : "Unknown failure"));
        });

        AppExecutor.getExecutor().submit(apiTask);
    }

    @FXML
    private void handleLoadFallback() {
        Task<ApiFetchResult> fallbackTask = new Task<>() {
            @Override
            protected ApiFetchResult call() {
                return apiEventService.loadFallbackEvents();
            }
        };

        fallbackTask.setOnSucceeded(e -> {
            ApiFetchResult res = fallbackTask.getValue();
            statusLabel.setStyle("-fx-text-fill: #0f766e;");
            statusLabel.setText(res.message());
            apiEventsTable.setItems(FXCollections.observableArrayList(res.events()));
            rawJsonArea.setText(res.rawJsonPreview());
        });

        AppExecutor.getExecutor().submit(fallbackTask);
    }

    @FXML
    private void handleSaveSelectedToSqlite() {
        ApiEvent selected = apiEventsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select an API Event row in the table to import into SQLite.");
            return;
        }

        Event localEvent = new Event(
                selected.getTitle(),
                selected.getBody(),
                selected.getCategory(),
                selected.getDate(),
                "10:00",
                "13:00",
                selected.getVenue(),
                selected.getOrganizer(),
                selected.getMaxParticipants(),
                "Upcoming"
        );

        Task<Boolean> saveTask = new Task<>() {
            @Override
            protected Boolean call() {
                return eventService.createEvent(localEvent);
            }
        };

        saveTask.setOnSucceeded(e -> {
            statusLabel.setStyle("-fx-text-fill: #16a34a;");
            statusLabel.setText("Saved API event '" + selected.getTitle() + "' to local SQLite database!");
            showAlert(Alert.AlertType.INFORMATION, "Saved to SQLite",
                    "The API event '" + selected.getTitle() + "' has been saved to your local SQLite Events table.");
        });

        saveTask.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Database Error", saveTask.getException().getMessage()));
        AppExecutor.getExecutor().submit(saveTask);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToDashboard() {
        SceneNavigator.navigateTo("dashboard.fxml", "Dashboard");
    }

    @FXML
    private void goToEvents() {
        SceneNavigator.navigateTo("events.fxml", "Event Management");
    }

    @FXML
    private void goToParticipants() {
        SceneNavigator.navigateTo("participants.fxml", "Participants & Attendance");
    }

    @FXML
    private void goToSchedule() {
        SceneNavigator.navigateTo("schedule.fxml", "Schedule Management");
    }

    @FXML
    private void goToTasks() {
        SceneNavigator.navigateTo("tasks.fxml", "Task Management");
    }

    @FXML
    private void goToLeaderboard() {
        SceneNavigator.navigateTo("leaderboard.fxml", "Leaderboard & Results");
    }

    @FXML
    private void goToApiEvents() {
        SceneNavigator.navigateTo("api-events.fxml", "API Events");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        SceneNavigator.navigateTo("login.fxml", "Login");
    }
}
