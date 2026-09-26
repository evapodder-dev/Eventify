package com.eventify.controller;

import com.eventify.dao.EventDAO;
import com.eventify.dao.TaskDAO;
import com.eventify.model.Event;
import com.eventify.model.Task;
import com.eventify.util.AppExecutor;
import com.eventify.util.SceneNavigator;
import com.eventify.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for tasks.fxml handling Task CRUD, status completion, and filtering.
 */
public class TaskController {

    @FXML
    private ComboBox<Event> eventCombo;
    @FXML
    private TextField taskNameField;
    @FXML
    private TextField assignedToField;
    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private ComboBox<String> priorityCombo;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private ComboBox<String> filterPriorityCombo;
    @FXML
    private ComboBox<String> filterStatusCombo;
    @FXML
    private Label statusLabel;

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Number> colId;
    @FXML
    private TableColumn<Task, String> colEvent;
    @FXML
    private TableColumn<Task, String> colTaskName;
    @FXML
    private TableColumn<Task, String> colAssignedTo;
    @FXML
    private TableColumn<Task, String> colDeadline;
    @FXML
    private TableColumn<Task, String> colPriority;
    @FXML
    private TableColumn<Task, String> colStatus;

    private final TaskDAO taskDAO = new TaskDAO();
    private final EventDAO eventDAO = new EventDAO();
    private Task selectedTask;

    @FXML
    public void initialize() {
        setupNavigationHeader();
        priorityCombo.setItems(FXCollections.observableArrayList("Low", "Medium", "High"));
        priorityCombo.setValue("Medium");
        statusCombo.setItems(FXCollections.observableArrayList("Pending", "In Progress", "Completed"));
        statusCombo.setValue("Pending");

        filterPriorityCombo.setItems(FXCollections.observableArrayList("All", "Low", "Medium", "High"));
        filterPriorityCombo.setValue("All");
        filterStatusCombo.setItems(FXCollections.observableArrayList("All", "Pending", "In Progress", "Completed"));
        filterStatusCombo.setValue("All");

        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTaskId()));
        colEvent.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().getEventId() + " - " + c.getValue().getEventName()));
        colTaskName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTaskName()));
        colAssignedTo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssignedTo()));
        colDeadline.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeadline()));
        colPriority.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriority()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedTask = newV;
                taskNameField.setText(newV.getTaskName());
                assignedToField.setText(newV.getAssignedTo());
                try {
                    deadlinePicker.setValue(LocalDate.parse(newV.getDeadline()));
                } catch (Exception ignored) {
                    deadlinePicker.setValue(null);
                }
                priorityCombo.setValue(newV.getPriority());
                statusCombo.setValue(newV.getStatus());
                for (Event ev : eventCombo.getItems()) {
                    if (ev.getEventId() == newV.getEventId()) {
                        eventCombo.setValue(ev);
                        break;
                    }
                }
            }
        });

        loadTasks();
    }

    @FXML
    public void loadTasks() {
        filterPriorityCombo.setValue("All");
        filterStatusCombo.setValue("All");

        javafx.concurrent.Task<Void> bgTask = new javafx.concurrent.Task<>() {
            List<Event> events;
            List<Task> tasks;

            @Override
            protected Void call() {
                events = eventDAO.findAll();
                tasks = taskDAO.findAll();
                return null;
            }

            @Override
            protected void succeeded() {
                eventCombo.setItems(FXCollections.observableArrayList(events));
                tasksTable.setItems(FXCollections.observableArrayList(tasks));
                statusLabel.setText("Loaded " + tasks.size() + " coordination task(s).");
            }
        };
        AppExecutor.getExecutor().submit(bgTask);
    }

    @FXML
    private void handleFilterTasks() {
        String p = filterPriorityCombo.getValue();
        String s = filterStatusCombo.getValue();
        javafx.concurrent.Task<List<Task>> bgTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Task> call() {
                return taskDAO.filterTasks(p, s);
            }
        };
        bgTask.setOnSucceeded(e -> {
            tasksTable.setItems(FXCollections.observableArrayList(bgTask.getValue()));
            statusLabel.setText("Filtered to " + bgTask.getValue().size() + " task(s).");
        });
        AppExecutor.getExecutor().submit(bgTask);
    }

    @FXML
    private void handleAddTask() {
        try {
            Task t = buildFromForm(0);
            javafx.concurrent.Task<Boolean> bgTask = new javafx.concurrent.Task<>() {
                @Override
                protected Boolean call() {
                    return taskDAO.insert(t);
                }
            };
            bgTask.setOnSucceeded(e -> {
                handleClearForm();
                loadTasks();
                statusLabel.setText("Task added.");
            });
            bgTask.setOnFailed(e -> showError("Add Task Error", bgTask.getException().getMessage()));
            AppExecutor.getExecutor().submit(bgTask);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }

    @FXML
    private void handleUpdateTask() {
        if (selectedTask == null) {
            showError("No Selection", "Select a task from the table to update.");
            return;
        }
        try {
            Task t = buildFromForm(selectedTask.getTaskId());
            javafx.concurrent.Task<Boolean> bgTask = new javafx.concurrent.Task<>() {
                @Override
                protected Boolean call() {
                    return taskDAO.update(t);
                }
            };
            bgTask.setOnSucceeded(e -> {
                handleClearForm();
                loadTasks();
                statusLabel.setText("Task updated.");
            });
            bgTask.setOnFailed(e -> showError("Update Task Error", bgTask.getException().getMessage()));
            AppExecutor.getExecutor().submit(bgTask);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }

    @FXML
    private void handleCompleteTask() {
        if (selectedTask == null) {
            showError("No Selection", "Select a task from the table to mark as Completed.");
            return;
        }
        int id = selectedTask.getTaskId();
        javafx.concurrent.Task<Boolean> bgTask = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() {
                return taskDAO.markCompleted(id);
            }
        };
        bgTask.setOnSucceeded(e -> {
            handleClearForm();
            loadTasks();
            statusLabel.setText("Task #" + id + " marked as Completed.");
        });
        bgTask.setOnFailed(e -> showError("Complete Task Error", bgTask.getException().getMessage()));
        AppExecutor.getExecutor().submit(bgTask);
    }

    @FXML
    private void handleDeleteTask() {
        if (selectedTask == null) {
            showError("No Selection", "Select a task from the table to delete.");
            return;
        }
        int id = selectedTask.getTaskId();
        javafx.concurrent.Task<Boolean> bgTask = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() {
                return taskDAO.delete(id);
            }
        };
        bgTask.setOnSucceeded(e -> {
            handleClearForm();
            loadTasks();
            statusLabel.setText("Task deleted.");
        });
        bgTask.setOnFailed(e -> showError("Delete Task Error", bgTask.getException().getMessage()));
        AppExecutor.getExecutor().submit(bgTask);
    }

    @FXML
    public void handleClearForm() {
        selectedTask = null;
        tasksTable.getSelectionModel().clearSelection();
        eventCombo.setValue(null);
        taskNameField.clear();
        assignedToField.clear();
        deadlinePicker.setValue(null);
        priorityCombo.setValue("Medium");
        statusCombo.setValue("Pending");
    }

    private Task buildFromForm(int id) {
        Event ev = eventCombo.getValue();
        String name = taskNameField.getText() != null ? taskNameField.getText().trim() : "";
        String assigned = assignedToField.getText() != null ? assignedToField.getText().trim() : "";
        String deadline = deadlinePicker.getValue() != null ? deadlinePicker.getValue().toString() : "";
        String priority = priorityCombo.getValue();
        String status = statusCombo.getValue();

        if (ev == null || name.isEmpty() || assigned.isEmpty() || deadline.isEmpty() || priority == null || status == null) {
            throw new IllegalArgumentException("All task fields are required.");
        }
        return new Task(id, ev.getEventId(), ev.getEventName(), name, assigned, deadline, priority, status);
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

