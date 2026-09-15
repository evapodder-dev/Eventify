package com.eventify.controller;

import com.eventify.model.Event;
import com.eventify.model.User;
import com.eventify.service.DashboardService;
import com.eventify.service.DashboardService.DashboardSummary;
import com.eventify.util.AppExecutor;
import com.eventify.util.SceneNavigator;
import com.eventify.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for dashboard.fxml.
 * Loads SQLite statistics in the background using JavaFX Task + ExecutorService.
 */
public class DashboardController {

    @FXML
    private Label userRoleLabel;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label accessSummaryLabel;
    @FXML
    private Label totalEventsLabel;
    @FXML
    private Label upcomingEventsLabel;
    @FXML
    private Label ongoingEventsLabel;
    @FXML
    private Label totalParticipantsLabel;
    @FXML
    private Label pendingTasksLabel;
    @FXML
    private Label threadStatusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private TableView<Event> recentEventsTable;
    @FXML
    private TableColumn<Event, Number> colId;
    @FXML
    private TableColumn<Event, String> colName;
    @FXML
    private TableColumn<Event, String> colCategory;
    @FXML
    private TableColumn<Event, String> colDate;
    @FXML
    private TableColumn<Event, String> colVenue;
    @FXML
    private TableColumn<Event, String> colOrganizer;
    @FXML
    private TableColumn<Event, String> colStatus;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userRoleLabel.setText("Signed in: " + currentUser.getRole());
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
            accessSummaryLabel.setText(currentUser.getAccessSummary());
        }

        colId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getEventId()));
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEventName()));
        colCategory.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
        colVenue.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getVenue()));
        colOrganizer.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrganizer()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

        refreshDashboard();
    }

    @FXML
    public void refreshDashboard() {
        loadingIndicator.setVisible(true);
        threadStatusLabel.setText("Loading statistics on background worker thread...");

        Task<DashboardSummary> loadTask = new Task<>() {
            @Override
            protected DashboardSummary call() {
                return dashboardService.loadDashboardSummaryConcurrently();
            }
        };

        loadTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            DashboardSummary summary = loadTask.getValue();
            totalEventsLabel.setText(String.valueOf(summary.totalEvents()));
            upcomingEventsLabel.setText(String.valueOf(summary.upcomingEvents()));
            ongoingEventsLabel.setText(String.valueOf(summary.ongoingEvents()));
            totalParticipantsLabel.setText(String.valueOf(summary.totalParticipants()));
            pendingTasksLabel.setText(String.valueOf(summary.pendingTasks()));
            recentEventsTable.setItems(FXCollections.observableArrayList(summary.recentEvents()));
            threadStatusLabel.setText("Synchronized with SQLite (" + summary.recentEvents().size() + " events)");
        });

        loadTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            Throwable ex = loadTask.getException();
            threadStatusLabel.setText("Error loading stats: " + (ex != null ? ex.getMessage() : "Unknown"));
        });

        AppExecutor.getExecutor().submit(loadTask);
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
