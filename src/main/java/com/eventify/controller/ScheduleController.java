package com.eventify.controller;

import com.eventify.dao.EventDAO;
import com.eventify.dao.ScheduleDAO;
import com.eventify.model.Event;
import com.eventify.model.Schedule;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for schedule.fxml handling Schedule CRUD and event-based filtering.
 */
public class ScheduleController {

    @FXML
    private GridPane scheduleFormGrid;
    @FXML
    private ComboBox<Event> eventCombo;
    @FXML
    private TextField activityField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;
    @FXML
    private TextField venueField;
    @FXML
    private ComboBox<Event> filterEventCombo;
    @FXML
    private Label statusLabel;

    @FXML
    private Button addScheduleBtn;
    @FXML
    private Button updateScheduleBtn;
    @FXML
    private Button deleteScheduleBtn;
    @FXML
    private Button clearScheduleBtn;

    @FXML
    private TableView<Schedule> scheduleTable;
    @FXML
    private TableColumn<Schedule, String> colEvent;
    @FXML
    private TableColumn<Schedule, String> colActivity;
    @FXML
    private TableColumn<Schedule, String> colDate;
    @FXML
    private TableColumn<Schedule, String> colStart;
    @FXML
    private TableColumn<Schedule, String> colEnd;
    @FXML
    private TableColumn<Schedule, String> colVenue;

    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final EventDAO eventDAO = new EventDAO();
    private Schedule selectedSchedule;

    @FXML
    public void initialize() {
        setupNavigationHeader();
        applyRolePermissions();

        colEvent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEventName()));
        colActivity.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getActivityName()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate()));
        colStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartTime()));
        colEnd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEndTime()));
        colVenue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVenue()));

        scheduleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedSchedule = newV;
                activityField.setText(newV.getActivityName());
                try {
                    datePicker.setValue(LocalDate.parse(newV.getDate()));
                } catch (Exception ignored) {
                    datePicker.setValue(null);
                }
                startTimeField.setText(newV.getStartTime());
                endTimeField.setText(newV.getEndTime());
                venueField.setText(newV.getVenue());
                for (Event ev : eventCombo.getItems()) {
                    if (ev.getEventId() == newV.getEventId()) {
                        eventCombo.setValue(ev);
                        break;
                    }
                }
            }
        });

        // Responsive: bind schedule table columns to table width
        if (scheduleTable != null && scheduleTable.getColumns().size() >= 6) {
            ResponsiveHelper.bindColumnWidths(scheduleTable, 0.24, 0.24, 0.14, 0.10, 0.10, 0.18);
        }

        loadSchedules();
    }

    private void applyRolePermissions() {
        if (!SessionManager.isOrganizer()) {
            if (scheduleFormGrid != null) {
                scheduleFormGrid.setVisible(false);
                scheduleFormGrid.setManaged(false);
            }
            if (addScheduleBtn != null) {
                addScheduleBtn.setVisible(false);
                addScheduleBtn.setManaged(false);
            }
            if (updateScheduleBtn != null) {
                updateScheduleBtn.setVisible(false);
                updateScheduleBtn.setManaged(false);
            }
            if (deleteScheduleBtn != null) {
                deleteScheduleBtn.setVisible(false);
                deleteScheduleBtn.setManaged(false);
            }
            if (clearScheduleBtn != null) {
                clearScheduleBtn.setVisible(false);
                clearScheduleBtn.setManaged(false);
            }
        }
    }

    @FXML
    public void loadSchedules() {
        filterEventCombo.setValue(null);
        Task<Void> task = new Task<>() {
            List<Event> events;
            List<Schedule> schedules;

            @Override
            protected Void call() {
                events = eventDAO.findAll();
                schedules = scheduleDAO.findAll();
                return null;
            }

            @Override
            protected void succeeded() {
                eventCombo.setItems(FXCollections.observableArrayList(events));
                filterEventCombo.setItems(FXCollections.observableArrayList(events));
                scheduleTable.setItems(FXCollections.observableArrayList(schedules));
                statusLabel.setText("Loaded " + schedules.size() + " schedule item(s).");
                Integer contextId = SceneNavigator.consumeContextEventId();
                if (contextId != null) {
                    for (Event ev : events) {
                        if (ev.getEventId() == contextId) {
                            filterEventCombo.setValue(ev);
                            handleFilterByEvent();
                            break;
                        }
                    }
                }
            }
        };
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleFilterByEvent() {
        Event filterEv = filterEventCombo.getValue();
        if (filterEv == null) {
            return;
        }
        Task<List<Schedule>> task = new Task<>() {
            @Override
            protected List<Schedule> call() {
                return scheduleDAO.findByEventId(filterEv.getEventId());
            }
        };
        task.setOnSucceeded(e -> {
            scheduleTable.setItems(FXCollections.observableArrayList(task.getValue()));
            statusLabel.setText("Showing " + task.getValue().size() + " schedule item(s) for " + filterEv.getEventName());
        });
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleAddSchedule() {
        if (!SessionManager.isOrganizer()) {
            showError("Access Denied", "Only Organizer/Admin can add event schedules.");
            return;
        }
        try {
            Schedule s = buildFromForm(0);
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return scheduleDAO.insert(s);
                }
            };
            task.setOnSucceeded(e -> {
                handleClearForm();
                loadSchedules();
                statusLabel.setText("Schedule added.");
            });
            task.setOnFailed(e -> showError("Add Error", task.getException().getMessage()));
            AppExecutor.getExecutor().submit(task);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }

    @FXML
    private void handleUpdateSchedule() {
        if (!SessionManager.isOrganizer()) {
            showError("Access Denied", "Only Organizer/Admin can update event schedules.");
            return;
        }
        if (selectedSchedule == null) {
            showError("No Selection", "Select a schedule entry from the table to edit.");
            return;
        }
        try {
            Schedule s = buildFromForm(selectedSchedule.getScheduleId());
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return scheduleDAO.update(s);
                }
            };
            task.setOnSucceeded(e -> {
                handleClearForm();
                loadSchedules();
                statusLabel.setText("Schedule updated.");
            });
            task.setOnFailed(e -> showError("Update Error", task.getException().getMessage()));
            AppExecutor.getExecutor().submit(task);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteSchedule() {
        if (!SessionManager.isOrganizer()) {
            showError("Access Denied", "Only Organizer/Admin can delete event schedules.");
            return;
        }
        if (selectedSchedule == null) {
            showError("No Selection", "Select a schedule entry from the table to delete.");
            return;
        }
        int id = selectedSchedule.getScheduleId();
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return scheduleDAO.delete(id);
            }
        };
        task.setOnSucceeded(e -> {
            handleClearForm();
            loadSchedules();
            statusLabel.setText("Schedule deleted.");
        });
        task.setOnFailed(e -> showError("Delete Error", task.getException().getMessage()));
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleViewDetails() {
        if (selectedSchedule == null) {
            showError("No Selection", "Select a schedule entry from the table to view its full details.");
            return;
        }
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Schedule Details — " + selectedSchedule.getActivityName());
        info.setHeaderText("Activity: " + selectedSchedule.getActivityName());
        String details = String.format("""
                Event: %s
                Date: %s
                Time: %s - %s
                Venue: %s
                """,
                selectedSchedule.getEventName(),
                selectedSchedule.getDate(),
                selectedSchedule.getStartTime(),
                selectedSchedule.getEndTime(),
                selectedSchedule.getVenue()
        );
        info.setContentText(details);
        info.showAndWait();
    }

    @FXML
    public void handleClearForm() {
        selectedSchedule = null;
        scheduleTable.getSelectionModel().clearSelection();
        eventCombo.setValue(null);
        activityField.clear();
        datePicker.setValue(null);
        startTimeField.clear();
        endTimeField.clear();
        venueField.clear();
    }

    private Schedule buildFromForm(int id) {
        Event ev = eventCombo.getValue();
        String act = activityField.getText() != null ? activityField.getText().trim() : "";
        String dateStr = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
        String start = startTimeField.getText() != null ? startTimeField.getText().trim() : "";
        String end = endTimeField.getText() != null ? endTimeField.getText().trim() : "";
        String venue = venueField.getText() != null ? venueField.getText().trim() : "";

        if (ev == null || act.isEmpty() || dateStr.isEmpty() || start.isEmpty() || end.isEmpty() || venue.isEmpty()) {
            throw new IllegalArgumentException("All schedule fields are required.");
        }
        return new Schedule(id, ev.getEventId(), ev.getEventName(), act, dateStr, start, end, venue);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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

    @FXML
    private javafx.scene.control.Button backButton;
    @FXML
    private javafx.scene.control.Button forwardButton;
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
}
