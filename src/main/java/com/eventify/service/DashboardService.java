package com.eventify.service;

import com.eventify.dao.EventDAO;
import com.eventify.database.DatabaseConnection;
import com.eventify.exception.DatabaseException;
import com.eventify.model.Event;
import com.eventify.util.AppExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Service that calculates Dashboard statistics concurrently using ExecutorService and Future.
 * Demonstrates Java multithreading and concurrency as required by APL Topic Coverage.
 */
public class DashboardService {

    public record DashboardSummary(
            int totalEvents,
            int upcomingEvents,
            int ongoingEvents,
            int totalParticipants,
            int pendingTasks,
            List<Event> recentEvents
    ) {
    }

    private final EventDAO eventDAO = new EventDAO();

    public DashboardSummary loadDashboardSummaryConcurrently() {
        ExecutorService executor = AppExecutor.getExecutor();

        Future<Integer> totalEventsFuture = executor.submit(eventDAO::countAll);
        Future<Integer> upcomingEventsFuture = executor.submit(() -> eventDAO.countByStatus("Upcoming"));
        Future<Integer> ongoingEventsFuture = executor.submit(() -> eventDAO.countByStatus("Ongoing"));
        Future<Integer> totalParticipantsFuture = executor.submit(this::countParticipants);
        Future<Integer> pendingTasksFuture = executor.submit(this::countPendingTasks);
        Future<List<Event>> eventsListFuture = executor.submit(eventDAO::findAll);

        try {
            return new DashboardSummary(
                    totalEventsFuture.get(),
                    upcomingEventsFuture.get(),
                    ongoingEventsFuture.get(),
                    totalParticipantsFuture.get(),
                    pendingTasksFuture.get(),
                    eventsListFuture.get()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DatabaseException("Dashboard statistics calculation was interrupted", e);
        } catch (ExecutionException e) {
            throw new DatabaseException("Failed to compute dashboard statistics: " + e.getCause().getMessage(), e.getCause());
        }
    }

    private int countParticipants() {
        String sql = "SELECT COUNT(*) FROM participants";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error counting participants: " + e.getMessage(), e);
        }
    }

    private int countPendingTasks() {
        String sql = "SELECT COUNT(*) FROM tasks WHERE status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error counting pending tasks: " + e.getMessage(), e);
        }
    }
}
