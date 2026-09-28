package com.eventify.controller;

import com.eventify.dao.RegistrationDAO;
import com.eventify.model.Event;
import com.eventify.model.User;
import com.eventify.service.DashboardService;
import com.eventify.service.DashboardService.DashboardSummary;
import com.eventify.util.AppExecutor;
import com.eventify.util.ResponsiveHelper;
import com.eventify.util.SceneNavigator;
import com.eventify.util.SessionManager;
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
import javafx.scene.layout.VBox;

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
    private Button navEventsBtn;
    @FXML
    private Button navParticipantsBtn;
    @FXML
    private Button navTasksBtn;
    @FXML
    private VBox pendingTasksCard;
    @FXML
    private Button quickEventsBtn;
    @FXML
    private Button quickRegisterBtn;
    @FXML
    private Button quickParticipantsBtn;
    @FXML
    private Button quickTasksBtn;

    @FXML
    private TableView<Event> recentEventsTable;
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
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    @FXML
    public void initialize() {
        setupNavigationHeader();
        applyRolePermissions();

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userRoleLabel.setText("Signed in: " + currentUser.getRole() + " (" + currentUser.getUsername() + ")");
            if ("admin".equalsIgnoreCase(currentUser.getUsername())) {
                welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (EVA PODDER)");
                accessSummaryLabel.setText(currentUser.getAccessSummary());
            } else {
                welcomeLabel.setText("Welcome, " + currentUser.getUsername());
                accessSummaryLabel.setText("Participant Roll: " + currentUser.getUsername()
                        + " — Browse available events, register for Upcoming/Ongoing events, and view the Leaderboard.");
            }
        }

        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEventName()));
        colCategory.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
        colVenue.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getVenue()));
        colOrganizer.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrganizer()));
        colStatus.setCellValueFactory(cell -> {
            String st = cell.getValue().getStatus();
            if (!SessionManager.isOrganizer()) {
                if ("Upcoming".equalsIgnoreCase(st) || "Ongoing".equalsIgnoreCase(st)) {
                    return new SimpleStringProperty(st + " (Open)");
                }
                return new SimpleStringProperty(st + " (Closed)");
            }
            return new SimpleStringProperty(st);
        });

        // Responsive layout: bind table column widths to percentage of table width
        ResponsiveHelper.bindColumnWidths(recentEventsTable,
                0.26, 0.15, 0.12, 0.18, 0.13, 0.16);
        refreshDashboard();
    }

    private void applyRolePermissions() {
        boolean isAdmin = SessionManager.isOrganizer();
        if (!isAdmin) {
            if (navParticipantsBtn != null) {
                navParticipantsBtn.setVisible(false);
                navParticipantsBtn.setManaged(false);
            }
            if (navTasksBtn != null) {
                navTasksBtn.setVisible(false);
                navTasksBtn.setManaged(false);
            }
            if (quickParticipantsBtn != null) {
                quickParticipantsBtn.setVisible(false);
                quickParticipantsBtn.setManaged(false);
            }
            if (quickTasksBtn != null) {
                quickTasksBtn.setVisible(false);
                quickTasksBtn.setManaged(false);
            }
            if (pendingTasksCard != null) {
                pendingTasksCard.setVisible(false);
                pendingTasksCard.setManaged(false);
            }
            if (navEventsBtn != null) {
                navEventsBtn.setText("Events & Registration");
            }
            if (quickEventsBtn != null) {
                quickEventsBtn.setText("Events & Registration \u2192");
            }
        } else {
            if (quickRegisterBtn != null) {
                quickRegisterBtn.setVisible(false);
                quickRegisterBtn.setManaged(false);
            }
        }
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
    private void handleRegisterSelectedEvent() {
        Event selected = recentEventsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Select an Event");
            alert.setHeaderText("No Event Selected");
            alert.setContentText("Please select an Upcoming or Ongoing event from the table below to register.");
            alert.showAndWait();
            return;
        }
        if ("Completed".equalsIgnoreCase(selected.getStatus())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Registration Closed");
            alert.setHeaderText("Event Already Completed");
            alert.setContentText("Registration is closed for '" + selected.getEventName() + "' because it is Completed.\nYou can register for Upcoming or Ongoing events.");
            alert.showAndWait();
            return;
        }
        User user = SessionManager.getCurrentUser();
        String roll = user != null ? user.getUsername() : "2307032";

        Task<Boolean> regTask = new Task<>() {
            @Override
            protected Boolean call() {
                return registrationDAO.registerByRoll(roll, selected.getEventId());
            }
        };
        regTask.setOnSucceeded(e -> {
            refreshDashboard();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText("Registered Roll: " + roll);
            alert.setContentText("You (Roll: " + roll + ") are now registered for:\n" + selected.getEventName()
                    + "\nDate: " + selected.getDate() + " | Venue: " + selected.getVenue());
            alert.showAndWait();
        });
        regTask.setOnFailed(e -> {
            Throwable ex = regTask.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Notice");
            alert.setHeaderText("Could Not Register for " + selected.getEventName());
            alert.setContentText(ex != null ? ex.getMessage() : "Already registered or database error.");
            alert.showAndWait();
        });
        AppExecutor.getExecutor().submit(regTask);
    }

    @FXML
    private void goToDashboard() {
        SceneNavigator.navigateTo("dashboard.fxml", "Dashboard");
    }

    @FXML
    private void goToEvents() {
        SceneNavigator.navigateTo("events.fxml", SessionManager.isOrganizer() ? "Event Management" : "Events & Registration");
    }

    @FXML
    private void goToParticipants() {
        if (!SessionManager.isOrganizer()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Admin Only");
            alert.setHeaderText("Restricted to Organizer (admin)");
            alert.setContentText("Only Admin (EVA PODDER) can control Participants & Attendance.");
            alert.showAndWait();
            return;
        }
        SceneNavigator.navigateTo("participants.fxml", "Participants & Attendance");
    }

    @FXML
    private void goToSchedule() {
        SceneNavigator.navigateTo("schedule.fxml", "Schedule");
    }

    @FXML
    private void goToTasks() {
        if (!SessionManager.isOrganizer()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Admin Only");
            alert.setHeaderText("Restricted to Organizer (admin)");
            alert.setContentText("Only Admin (EVA PODDER) can manage organizer tasks.");
            alert.showAndWait();
            return;
        }
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

    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Label breadcrumbLabel;

    private void setupNavigationHeader() {
        if (backButton != null) {
            boolean canBack = SceneNavigator.canGoBack();
            backButton.setDisable(!canBack);
            String prev = SceneNavigator.getPreviousPageTitle();
            backButton.setText(canBack ? ("\u2190 Back (" + prev + ")") : "\u2190 Back");
        }
        if (forwardButton != null) {
            boolean canFwd = SceneNavigator.canGoForward();
            forwardButton.setDisable(!canFwd);
            String next = SceneNavigator.getNextPageTitle();
            forwardButton.setText(canFwd ? ("Forward (" + next + ") \u2192") : "Forward \u2192");
        }
        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText("Navigation Path:  " + SceneNavigator.getBreadcrumbTrail());
        }
    }

    @FXML
    private void handleGoBack() {
        SceneNavigator.goBack();
    }

    @FXML
    private void handleGoForward() {
        SceneNavigator.goForward();
    }

    @FXML
    private void handleJumpToSelectedEventSchedule() {
        Event selected = recentEventsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SceneNavigator.setContextEventId(selected.getEventId());
        }
        SceneNavigator.navigateTo("schedule.fxml", "Schedule");
    }
}


