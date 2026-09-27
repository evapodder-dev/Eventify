package com.eventify.controller;

import com.eventify.dao.EventDAO;
import com.eventify.dao.ParticipantDAO;
import com.eventify.dao.RegistrationDAO;
import com.eventify.model.Event;
import com.eventify.model.Participant;
import com.eventify.model.Registration;
import com.eventify.util.AppExecutor;
import com.eventify.util.ResponsiveHelper;
import com.eventify.util.SceneNavigator;
import com.eventify.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Controller for participants.fxml handling Participant CRUD, Event Registration, and Attendance Marking.
 */
public class ParticipantController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<String> departmentCombo;
    @FXML
    private ComboBox<String> yearCombo;
    @FXML
    private TextField searchParticipantField;
    @FXML
    private Label statusLabel;

    @FXML
    private TableView<Participant> participantsTable;
    @FXML
    private TableColumn<Participant, Number> colPartId;
    @FXML
    private TableColumn<Participant, String> colPartName;
    @FXML
    private TableColumn<Participant, String> colStudentId;
    @FXML
    private TableColumn<Participant, String> colEmail;
    @FXML
    private TableColumn<Participant, String> colPhone;
    @FXML
    private TableColumn<Participant, String> colDepartment;
    @FXML
    private TableColumn<Participant, String> colYear;

    @FXML
    private ComboBox<Participant> regParticipantCombo;
    @FXML
    private ComboBox<Event> regEventCombo;
    @FXML
    private TableView<Registration> registrationsTable;
    @FXML
    private TableColumn<Registration, Number> colRegId;
    @FXML
    private TableColumn<Registration, String> colRegParticipant;
    @FXML
    private TableColumn<Registration, String> colRegStudentId;
    @FXML
    private TableColumn<Registration, String> colRegEvent;
    @FXML
    private TableColumn<Registration, String> colRegDate;
    @FXML
    private TableColumn<Registration, String> colRegAttendance;

    private final ParticipantDAO participantDAO = new ParticipantDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private Participant selectedParticipant;

    @FXML
    public void initialize() {
        setupNavigationHeader();
        departmentCombo.setItems(FXCollections.observableArrayList("CSE", "EEE", "SWE", "ME", "CE", "BBA"));
        yearCombo.setItems(FXCollections.observableArrayList("1st Year", "2nd Year", "3rd Year", "4th Year"));

        colPartId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getParticipantId()));
        colPartName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colStudentId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentId()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colDepartment.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartment()));
        colYear.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getYear()));

        colRegId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getRegistrationId()));
        colRegParticipant.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getParticipantName()));
        colRegStudentId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentId()));
        colRegEvent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEventName()));
        colRegDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegistrationDate()));
        colRegAttendance.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAttendanceStatus()));

        participantsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedParticipant = newV;
                nameField.setText(newV.getName());
                studentIdField.setText(newV.getStudentId());
                emailField.setText(newV.getEmail());
                phoneField.setText(newV.getPhone());
                departmentCombo.setValue(newV.getDepartment());
                yearCombo.setValue(newV.getYear());
                regParticipantCombo.setValue(newV);
            }
        });

        // Responsive: bind participant and registration table columns to table width
        ResponsiveHelper.bindColumnWidths(participantsTable, 0.06, 0.16, 0.12, 0.18, 0.14, 0.18, 0.10);
        ResponsiveHelper.bindColumnWidths(registrationsTable, 0.08, 0.20, 0.14, 0.22, 0.18, 0.14);

        loadAllData();
    }

    private void loadAllData() {
        Task<Void> task = new Task<>() {
            List<Participant> participants;
            List<Event> events;
            List<Registration> registrations;

            @Override
            protected Void call() {
                participants = participantDAO.findAll();
                events = eventDAO.findAll();
                registrations = registrationDAO.findAll();
                return null;
            }

            @Override
            protected void succeeded() {
                participantsTable.setItems(FXCollections.observableArrayList(participants));
                regParticipantCombo.setItems(FXCollections.observableArrayList(participants));
                regEventCombo.setItems(FXCollections.observableArrayList(events));
                registrationsTable.setItems(FXCollections.observableArrayList(registrations));
                statusLabel.setText("Loaded " + participants.size() + " participants and " + registrations.size() + " registrations.");
            }
        };
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleAddParticipant() {
        try {
            Participant p = buildParticipantFromForm(0);
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return participantDAO.insert(p);
                }
            };
            task.setOnSucceeded(e -> {
                handleClearParticipantForm();
                loadAllData();
                statusLabel.setText("Participant added successfully.");
            });
            task.setOnFailed(e -> showError("Failed to Add Participant", task.getException().getMessage()));
            AppExecutor.getExecutor().submit(task);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }

    @FXML
    private void handleUpdateParticipant() {
        if (selectedParticipant == null) {
            showError("No Selection", "Select a participant from the table to update.");
            return;
        }
        try {
            Participant p = buildParticipantFromForm(selectedParticipant.getParticipantId());
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return participantDAO.update(p);
                }
            };
            task.setOnSucceeded(e -> {
                handleClearParticipantForm();
                loadAllData();
                statusLabel.setText("Participant updated.");
            });
            task.setOnFailed(e -> showError("Update Error", task.getException().getMessage()));
            AppExecutor.getExecutor().submit(task);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteParticipant() {
        if (selectedParticipant == null) {
            showError("No Selection", "Select a participant from the table to delete.");
            return;
        }
        int id = selectedParticipant.getParticipantId();
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return participantDAO.delete(id);
            }
        };
        task.setOnSucceeded(e -> {
            handleClearParticipantForm();
            loadAllData();
            statusLabel.setText("Participant deleted.");
        });
        task.setOnFailed(e -> showError("Delete Error", task.getException().getMessage()));
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleViewDetails() {
        if (selectedParticipant == null) {
            showError("No Selection", "Select a participant from the table to view details.");
            return;
        }
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Participant Details - #" + selectedParticipant.getParticipantId());
        info.setHeaderText("Name: " + selectedParticipant.getName());
        String details = String.format("""
                Participant ID: %d
                Student ID: %s
                Email: %s
                Phone: %s
                Department: %s
                Year: %s
                """,
                selectedParticipant.getParticipantId(),
                selectedParticipant.getStudentId(),
                selectedParticipant.getEmail(),
                selectedParticipant.getPhone(),
                selectedParticipant.getDepartment(),
                selectedParticipant.getYear()
        );
        info.setContentText(details);
        info.showAndWait();
    }


    @FXML
    private void handleSearchParticipants() {
        String keyword = searchParticipantField.getText();
        Task<List<Participant>> task = new Task<>() {
            @Override
            protected List<Participant> call() {
                return participantDAO.search(keyword);
            }
        };
        task.setOnSucceeded(e -> participantsTable.setItems(FXCollections.observableArrayList(task.getValue())));
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleViewRegisteredEvents() {
        if (selectedParticipant == null) {
            showError("Select Participant", "Select a participant first to filter their registered events.");
            return;
        }
        int pid = selectedParticipant.getParticipantId();
        Task<List<Registration>> task = new Task<>() {
            @Override
            protected List<Registration> call() {
                return registrationDAO.findByParticipant(pid);
            }
        };
        task.setOnSucceeded(e -> {
            registrationsTable.setItems(FXCollections.observableArrayList(task.getValue()));
            statusLabel.setText("Showing " + task.getValue().size() + " registered event(s) for " + selectedParticipant.getName());
        });
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleRegisterForEvent() {
        Participant p = regParticipantCombo.getValue();
        Event ev = regEventCombo.getValue();
        if (p == null || ev == null) {
            showError("Selection Required", "Please select both a Participant and an Event.");
            return;
        }
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return registrationDAO.registerParticipant(p.getParticipantId(), ev.getEventId());
            }
        };
        task.setOnSucceeded(e -> {
            loadRegistrations();
            statusLabel.setText("Registered " + p.getName() + " for " + ev.getEventName());
        });
        task.setOnFailed(e -> showError("Registration Failed", task.getException().getMessage()));
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleMarkPresent() {
        updateSelectedAttendance("Present");
    }

    @FXML
    private void handleMarkAbsent() {
        updateSelectedAttendance("Absent");
    }

    @FXML
    private void handleMarkNotMarked() {
        updateSelectedAttendance("Not Marked");
    }

    private void updateSelectedAttendance(String newStatus) {
        Registration selectedReg = registrationsTable.getSelectionModel().getSelectedItem();
        if (selectedReg == null) {
            showError("Select Registration", "Please select a registration row in the bottom table to mark attendance.");
            return;
        }
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return registrationDAO.updateAttendance(
                        selectedReg.getRegistrationId(),
                        selectedReg.getParticipantId(),
                        selectedReg.getEventId(),
                        newStatus
                );
            }
        };
        task.setOnSucceeded(e -> {
            loadRegistrations();
            statusLabel.setText("Attendance marked as '" + newStatus + "' for " + selectedReg.getParticipantName());
        });
        task.setOnFailed(e -> showError("Attendance Error", task.getException().getMessage()));
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    public void loadRegistrations() {
        Task<List<Registration>> task = new Task<>() {
            @Override
            protected List<Registration> call() {
                return registrationDAO.findAll();
            }
        };
        task.setOnSucceeded(e -> registrationsTable.setItems(FXCollections.observableArrayList(task.getValue())));
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    public void handleClearParticipantForm() {
        selectedParticipant = null;
        participantsTable.getSelectionModel().clearSelection();
        nameField.clear();
        studentIdField.clear();
        emailField.clear();
        phoneField.clear();
        departmentCombo.setValue(null);
        yearCombo.setValue(null);
    }

    private Participant buildParticipantFromForm(int id) {
        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        String sid = studentIdField.getText() != null ? studentIdField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
        String dept = departmentCombo.getValue();
        String yr = yearCombo.getValue();

        if (name.isEmpty() || sid.isEmpty() || email.isEmpty() || phone.isEmpty() || dept == null || yr == null) {
            throw new IllegalArgumentException("All participant fields are required.");
        }
        return new Participant(id, name, sid, email, phone, dept, yr);
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


