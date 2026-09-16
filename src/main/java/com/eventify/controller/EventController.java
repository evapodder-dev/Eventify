package com.eventify.controller;

import com.eventify.model.Event;
import com.eventify.service.EventService;
import com.eventify.util.AppExecutor;
import com.eventify.util.JsonUtil;
import com.eventify.util.SceneNavigator;
import com.eventify.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for events.fxml handling Event CRUD, search, details view, and JSON export/import.
 */
public class EventController {

    @FXML
    private TextField eventNameField;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;
    @FXML
    private TextField venueField;
    @FXML
    private TextField organizerField;
    @FXML
    private TextField maxParticipantsField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterStatusCombo;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private TableView<Event> eventsTable;
    @FXML
    private TableColumn<Event, Number> colId;
    @FXML
    private TableColumn<Event, String> colName;
    @FXML
    private TableColumn<Event, String> colCategory;
    @FXML
    private TableColumn<Event, String> colDate;
    @FXML
    private TableColumn<Event, String> colStart;
    @FXML
    private TableColumn<Event, String> colEnd;
    @FXML
    private TableColumn<Event, String> colVenue;
    @FXML
    private TableColumn<Event, String> colOrganizer;
    @FXML
    private TableColumn<Event, Number> colMax;
    @FXML
    private TableColumn<Event, String> colStatus;

    private final EventService eventService = new EventService();
    private Event selectedEvent;

    @FXML
    public void initialize() {
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Workshop", "Programming Contest", "Seminar", "Competition", "Club Program", "Cultural Event"
        ));
        statusCombo.setItems(FXCollections.observableArrayList("Upcoming", "Ongoing", "Completed"));
        statusCombo.setValue("Upcoming");

        filterStatusCombo.setItems(FXCollections.observableArrayList("All", "Upcoming", "Ongoing", "Completed"));
        filterStatusCombo.setValue("All");

        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEventId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEventName()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate()));
        colStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartTime()));
        colEnd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEndTime()));
        colVenue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVenue()));
        colOrganizer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrganizer()));
        colMax.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMaxParticipants()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        eventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
            }
        });

        loadEvents();
    }

    @FXML
    public void loadEvents() {
        searchField.clear();
        filterStatusCombo.setValue("All");
        loadingIndicator.setVisible(true);

        Task<List<Event>> task = new Task<>() {
            @Override
            protected List<Event> call() {
                return eventService.getAllEvents();
            }
        };

        task.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            eventsTable.setItems(FXCollections.observableArrayList(task.getValue()));
            statusLabel.setText("Loaded " + task.getValue().size() + " university events from SQLite.");
        });

        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            showError("Load Error", task.getException().getMessage());
        });

        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        String statusFilter = filterStatusCombo.getValue();

        Task<List<Event>> searchTask = new Task<>() {
            @Override
            protected List<Event> call() {
                return eventService.searchEvents(keyword, statusFilter);
            }
        };

        searchTask.setOnSucceeded(e -> {
            eventsTable.setItems(FXCollections.observableArrayList(searchTask.getValue()));
            statusLabel.setText("Found " + searchTask.getValue().size() + " matching event(s).");
        });

        AppExecutor.getExecutor().submit(searchTask);
    }

    @FXML
    private void handleAddEvent() {
        try {
            Event event = buildEventFromForm(0);
            Task<Boolean> addTask = new Task<>() {
                @Override
                protected Boolean call() {
                    return eventService.createEvent(event);
                }
            };
            addTask.setOnSucceeded(e -> {
                handleClearForm();
                loadEvents();
                statusLabel.setText("Event added successfully.");
            });
            addTask.setOnFailed(e -> showError("Failed to Add Event", addTask.getException().getMessage()));
            AppExecutor.getExecutor().submit(addTask);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }

    @FXML
    private void handleUpdateEvent() {
        if (selectedEvent == null) {
            showError("No Selection", "Please select an event from the table to update.");
            return;
        }
        try {
            Event updated = buildEventFromForm(selectedEvent.getEventId());
            Task<Boolean> updateTask = new Task<>() {
                @Override
                protected Boolean call() {
                    return eventService.updateEvent(updated);
                }
            };
            updateTask.setOnSucceeded(e -> {
                handleClearForm();
                loadEvents();
                statusLabel.setText("Event #" + updated.getEventId() + " updated.");
            });
            updateTask.setOnFailed(e -> showError("Update Error", updateTask.getException().getMessage()));
            AppExecutor.getExecutor().submit(updateTask);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteEvent() {
        if (selectedEvent == null) {
            showError("No Selection", "Please select an event from the table to delete.");
            return;
        }
        int idToDelete = selectedEvent.getEventId();
        Task<Boolean> deleteTask = new Task<>() {
            @Override
            protected Boolean call() {
                return eventService.deleteEvent(idToDelete);
            }
        };
        deleteTask.setOnSucceeded(e -> {
            handleClearForm();
            loadEvents();
            statusLabel.setText("Deleted event #" + idToDelete + ".");
        });
        deleteTask.setOnFailed(e -> showError("Delete Error", deleteTask.getException().getMessage()));
        AppExecutor.getExecutor().submit(deleteTask);
    }

    @FXML
    private void handleViewDetails() {
        if (selectedEvent == null) {
            showError("No Selection", "Select an event in the table to view its full details.");
            return;
        }
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Event Details — #" + selectedEvent.getEventId());
        info.setHeaderText(selectedEvent.getEventName() + " (" + selectedEvent.getCategory() + ")");
        String details = String.format("""
                Event ID: %d
                Status: %s
                Date: %s
                Time: %s - %s
                Venue: %s
                Organizer: %s
                Maximum Participants: %d
                
                Description:
                %s
                """,
                selectedEvent.getEventId(),
                selectedEvent.getStatus(),
                selectedEvent.getDate(),
                selectedEvent.getStartTime(),
                selectedEvent.getEndTime(),
                selectedEvent.getVenue(),
                selectedEvent.getOrganizer(),
                selectedEvent.getMaxParticipants(),
                selectedEvent.getDescription() != null ? selectedEvent.getDescription() : "N/A"
        );
        info.setContentText(details);
        info.showAndWait();
    }

    @FXML
    private void handleExportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Event(s) to JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName(selectedEvent != null ? "event-" + selectedEvent.getEventId() + ".json" : "events-export.json");
        File file = chooser.showSaveDialog(SceneNavigator.getPrimaryStage());
        if (file == null) {
            return;
        }

        Event targetSingle = selectedEvent;
        List<Event> allCurrent = List.copyOf(eventsTable.getItems());

        Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (targetSingle != null) {
                    JsonUtil.exportEventToFile(targetSingle, file);
                } else {
                    JsonUtil.exportEventListToFile(allCurrent, file);
                }
                return null;
            }
        };

        exportTask.setOnSucceeded(e -> statusLabel.setText("Exported JSON to: " + file.getName()));
        exportTask.setOnFailed(e -> showError("JSON Export Error", exportTask.getException().getMessage()));
        AppExecutor.getExecutor().submit(exportTask);
    }

    @FXML
    private void handleImportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Event(s) from JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showOpenDialog(SceneNavigator.getPrimaryStage());
        if (file == null) {
            return;
        }

        Task<Integer> importTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                List<Event> imported = JsonUtil.importEventsFromFile(file);
                int count = 0;
                for (Event ev : imported) {
                    if (ev.getStartTime() == null || ev.getStartTime().isBlank()) ev.setStartTime("10:00");
                    if (ev.getEndTime() == null || ev.getEndTime().isBlank()) ev.setEndTime("13:00");
                    if (ev.getOrganizer() == null || ev.getOrganizer().isBlank()) ev.setOrganizer("University Club");
                    if (ev.getStatus() == null || ev.getStatus().isBlank()) ev.setStatus("Upcoming");
                    if (ev.getDescription() == null) ev.setDescription("Imported from JSON");
                    if (ev.getMaxParticipants() <= 0) ev.setMaxParticipants(100);
                    if (eventService.createEvent(ev)) {
                        count++;
                    }
                }
                return count;
            }
        };

        importTask.setOnSucceeded(e -> {
            loadEvents();
            statusLabel.setText("Successfully imported " + importTask.getValue() + " event(s) from JSON.");
        });
        importTask.setOnFailed(e -> showError("JSON Import Error", importTask.getException().getMessage()));
        AppExecutor.getExecutor().submit(importTask);
    }

    @FXML
    public void handleClearForm() {
        selectedEvent = null;
        eventsTable.getSelectionModel().clearSelection();
        eventNameField.clear();
        categoryCombo.setValue(null);
        datePicker.setValue(null);
        statusCombo.setValue("Upcoming");
        startTimeField.clear();
        endTimeField.clear();
        venueField.clear();
        organizerField.clear();
        maxParticipantsField.clear();
        descriptionField.clear();
    }

    private void populateForm(Event event) {
        selectedEvent = event;
        eventNameField.setText(event.getEventName());
        categoryCombo.setValue(event.getCategory());
        try {
            datePicker.setValue(LocalDate.parse(event.getDate()));
        } catch (Exception ignored) {
            datePicker.setValue(null);
        }
        statusCombo.setValue(event.getStatus());
        startTimeField.setText(event.getStartTime());
        endTimeField.setText(event.getEndTime());
        venueField.setText(event.getVenue());
        organizerField.setText(event.getOrganizer());
        maxParticipantsField.setText(String.valueOf(event.getMaxParticipants()));
        descriptionField.setText(event.getDescription());
    }

    private Event buildEventFromForm(int id) {
        int maxPart;
        try {
            maxPart = Integer.parseInt(maxParticipantsField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Maximum Participants must be a valid integer.");
        }
        String dateStr = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
        return new Event(
                id,
                eventNameField.getText().trim(),
                descriptionField.getText() != null ? descriptionField.getText().trim() : "",
                categoryCombo.getValue(),
                dateStr,
                startTimeField.getText().trim(),
                endTimeField.getText().trim(),
                venueField.getText().trim(),
                organizerField.getText().trim(),
                maxPart,
                statusCombo.getValue()
        );
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
}
