package com.eventify.controller;

import com.eventify.dao.EventDAO;
import com.eventify.dao.ParticipantDAO;
import com.eventify.dao.ResultDAO;
import com.eventify.model.Event;
import com.eventify.model.Participant;
import com.eventify.model.Result;
import com.eventify.util.AppExecutor;
import com.eventify.util.ResponsiveHelper;
import com.eventify.util.SceneNavigator;
import com.eventify.util.SessionManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.List;

/**
 * Controller for leaderboard.fxml managing participant scores and dynamic/custom rankings.
 */
public class LeaderboardController {

    @FXML
    private GridPane leaderboardFormGrid;
    @FXML
    private ComboBox<Event> eventCombo;
    @FXML
    private ComboBox<Participant> participantCombo;
    @FXML
    private TextField scoreField;
    @FXML
    private TextField rankField;
    @FXML
    private ComboBox<Event> filterEventCombo;
    @FXML
    private Label statusLabel;

    @FXML
    private Button addResultBtn;
    @FXML
    private Button updateResultBtn;
    @FXML
    private Button deleteResultBtn;
    @FXML
    private Button clearResultBtn;

    @FXML
    private TableView<Result> leaderboardTable;
    @FXML
    private TableColumn<Result, Number> colRank;
    @FXML
    private TableColumn<Result, String> colParticipant;
    @FXML
    private TableColumn<Result, String> colStudentId;
    @FXML
    private TableColumn<Result, Number> colScore;
    @FXML
    private TableColumn<Result, String> colEvent;

    private final ResultDAO resultDAO = new ResultDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final ParticipantDAO participantDAO = new ParticipantDAO();
    private Result selectedResult;

    @FXML
    public void initialize() {
        setupNavigationHeader();
        applyRolePermissions();

        colRank.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getRank()));
        colStudentId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentId()));
        colParticipant.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getParticipantName()));
        colScore.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getScore()));
        colEvent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEventName()));

        leaderboardTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedResult = newV;
                scoreField.setText(String.valueOf(newV.getScore()));
                if (rankField != null) {
                    rankField.setText(String.valueOf(newV.getRank()));
                }
                for (Event ev : eventCombo.getItems()) {
                    if (ev.getEventId() == newV.getEventId()) {
                        eventCombo.setValue(ev);
                        break;
                    }
                }
                for (Participant p : participantCombo.getItems()) {
                    if (p.getParticipantId() == newV.getParticipantId()) {
                        participantCombo.setValue(p);
                        break;
                    }
                }
            }
        });

        // Responsive: bind leaderboard table columns to table width
        if (leaderboardTable != null && leaderboardTable.getColumns().size() >= 5) {
            ResponsiveHelper.bindColumnWidths(leaderboardTable, 0.10, 0.18, 0.24, 0.16, 0.32);
        }

        loadResults();
    }

    private void applyRolePermissions() {
        if (!SessionManager.isOrganizer()) {
            if (leaderboardFormGrid != null) {
                leaderboardFormGrid.setVisible(false);
                leaderboardFormGrid.setManaged(false);
            }
            if (addResultBtn != null) {
                addResultBtn.setVisible(false);
                addResultBtn.setManaged(false);
            }
            if (updateResultBtn != null) {
                updateResultBtn.setVisible(false);
                updateResultBtn.setManaged(false);
            }
            if (deleteResultBtn != null) {
                deleteResultBtn.setVisible(false);
                deleteResultBtn.setManaged(false);
            }
            if (clearResultBtn != null) {
                clearResultBtn.setVisible(false);
                clearResultBtn.setManaged(false);
            }
        }
    }

    @FXML
    public void loadResults() {
        filterEventCombo.setValue(null);
        Task<Void> task = new Task<>() {
            List<Event> events;
            List<Participant> participants;
            List<Result> results;

            @Override
            protected Void call() {
                events = eventDAO.findAll();
                participants = participantDAO.findAll();
                results = resultDAO.findAll();
                return null;
            }

            @Override
            protected void succeeded() {
                eventCombo.setItems(FXCollections.observableArrayList(events));
                filterEventCombo.setItems(FXCollections.observableArrayList(events));
                participantCombo.setItems(FXCollections.observableArrayList(participants));
                leaderboardTable.setItems(FXCollections.observableArrayList(results));
                statusLabel.setText("Loaded " + results.size() + " leaderboard result(s).");
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
        Event ev = filterEventCombo.getValue();
        if (ev == null) {
            return;
        }
        Task<List<Result>> task = new Task<>() {
            @Override
            protected List<Result> call() {
                return resultDAO.findByEventId(ev.getEventId());
            }
        };
        task.setOnSucceeded(e -> {
            leaderboardTable.setItems(FXCollections.observableArrayList(task.getValue()));
            statusLabel.setText("Leaderboard for " + ev.getEventName() + " (" + task.getValue().size() + " participants)");
        });
        AppExecutor.getExecutor().submit(task);
    }

    private int parseCustomRankForAdd() {
        if (rankField == null || rankField.getText() == null || rankField.getText().trim().isEmpty()) {
            return 0; // auto-calculate by score
        }
        String raw = rankField.getText().trim();
        try {
            int parsed = Integer.parseInt(raw);
            if (parsed <= 0) {
                throw new IllegalArgumentException("Rank must be a positive integer (1, 2, 3...) or left empty for auto-ranking.");
            }
            if (selectedResult != null && parsed == selectedResult.getRank()) {
                return 0;
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rank must be a valid integer or left empty for auto-ranking.");
        }
    }

    private int parseCustomRankForUpdate(double newScore) {
        if (rankField == null || rankField.getText() == null || rankField.getText().trim().isEmpty()) {
            return 0; // auto-calculate by score
        }
        String raw = rankField.getText().trim();
        try {
            int parsed = Integer.parseInt(raw);
            if (parsed <= 0) {
                throw new IllegalArgumentException("Rank must be a positive integer (1, 2, 3...) or left empty for auto-ranking.");
            }
            if (selectedResult != null
                    && parsed == selectedResult.getRank()
                    && Double.compare(newScore, selectedResult.getScore()) != 0) {
                return 0;
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rank must be a valid integer or left empty for auto-ranking.");
        }
    }

    @FXML
    private void handleSaveResult() {
        if (!SessionManager.isOrganizer()) {
            showError("Access Denied", "Only Organizer/Admin can add leaderboard scores.");
            return;
        }
        Event ev = eventCombo.getValue();
        Participant p = participantCombo.getValue();
        if (ev == null || p == null) {
            showError("Validation Error", "Please select both an Event and a Participant.");
            return;
        }
        double score;
        try {
            score = Double.parseDouble(scoreField.getText().trim());
        } catch (Exception e) {
            showError("Validation Error", "Score must be a valid number.");
            return;
        }
        int rankToUse;
        try {
            rankToUse = parseCustomRankForAdd();
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
            return;
        }

        Result r = new Result(0, ev.getEventId(), ev.getEventName(), p.getParticipantId(), p.getName(), p.getStudentId(), score, rankToUse);
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return resultDAO.insert(r);
            }
        };
        task.setOnSucceeded(e -> {
            handleClearForm();
            loadResults();
            statusLabel.setText(rankToUse > 0 ? "Result added with custom Rank #" + rankToUse + "." : "Score saved and ranks recalculated.");
        });
        task.setOnFailed(e -> showError("Save Result Error", task.getException().getMessage()));
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleUpdateResult() {
        if (!SessionManager.isOrganizer()) {
            showError("Access Denied", "Only Organizer/Admin can update leaderboard scores.");
            return;
        }
        if (selectedResult == null) {
            showError("No Selection", "Please select a result row from the leaderboard to update.");
            return;
        }
        Event ev = eventCombo.getValue();
        Participant p = participantCombo.getValue();
        if (ev == null || p == null) {
            showError("Validation Error", "Please select both an Event and a Participant.");
            return;
        }
        double score;
        try {
            score = Double.parseDouble(scoreField.getText().trim());
        } catch (Exception e) {
            showError("Validation Error", "Score must be a valid number.");
            return;
        }
        int rankToUse;
        try {
            rankToUse = parseCustomRankForUpdate(score);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
            return;
        }

        Result updated = new Result(
                selectedResult.getResultId(),
                ev.getEventId(),
                ev.getEventName(),
                p.getParticipantId(),
                p.getName(),
                p.getStudentId(),
                score,
                rankToUse
        );
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return resultDAO.update(updated);
            }
        };
        task.setOnSucceeded(e -> {
            handleClearForm();
            loadResults();
            statusLabel.setText(rankToUse > 0 ? "Result updated with Rank #" + rankToUse + "." : "Score updated and ranks recalculated.");
        });
        task.setOnFailed(e -> showError("Update Error", task.getException().getMessage()));
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleDeleteResult() {
        if (!SessionManager.isOrganizer()) {
            showError("Access Denied", "Only Organizer/Admin can delete leaderboard results.");
            return;
        }
        if (selectedResult == null) {
            showError("No Selection", "Select a result row from the leaderboard to delete.");
            return;
        }
        int id = selectedResult.getResultId();
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return resultDAO.delete(id);
            }
        };
        task.setOnSucceeded(e -> {
            handleClearForm();
            loadResults();
            statusLabel.setText("Result deleted and ranks recalculated.");
        });
        task.setOnFailed(e -> showError("Delete Error", task.getException().getMessage()));
        AppExecutor.getExecutor().submit(task);
    }

    @FXML
    private void handleViewDetails() {
        if (selectedResult == null) {
            showError("No Selection", "Select a result row from the leaderboard to view details.");
            return;
        }
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Leaderboard Result Details — Roll: " + selectedResult.getStudentId());
        info.setHeaderText("Rank #" + selectedResult.getRank() + " — " + selectedResult.getParticipantName());
        String details = String.format("""
                Rank: #%d
                Roll: %s
                Participant: %s
                Event: %s
                Score: %.2f
                """,
                selectedResult.getRank(),
                selectedResult.getStudentId(),
                selectedResult.getParticipantName(),
                selectedResult.getEventName(),
                selectedResult.getScore()
        );
        info.setContentText(details);
        info.showAndWait();
    }

    @FXML
    public void handleClearForm() {
        selectedResult = null;
        leaderboardTable.getSelectionModel().clearSelection();
        eventCombo.setValue(null);
        participantCombo.setValue(null);
        scoreField.clear();
        if (rankField != null) {
            rankField.clear();
        }
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
