package com.eventify.dao;

import com.eventify.database.DatabaseConnection;
import com.eventify.exception.DatabaseException;
import com.eventify.model.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for SQLite CRUD operations on the events table.
 */
public class EventDAO implements CrudDAO<Event, Integer> {

    @Override
    public boolean insert(Event event) {
        String sql = """
            INSERT INTO events (event_name, description, category, event_date, start_time, end_time,
                                venue, organizer, max_participants, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindEventParameters(ps, event);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert event: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Event event) {
        String sql = """
            UPDATE events
            SET event_name = ?, description = ?, category = ?, event_date = ?,
                start_time = ?, end_time = ?, venue = ?, organizer = ?,
                max_participants = ?, status = ?
            WHERE event_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindEventParameters(ps, event);
            ps.setInt(11, event.getEventId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update event: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM events WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete event: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Event> findById(Integer id) {
        String sql = "SELECT * FROM events WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find event by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Event> findAll() {
        String sql = "SELECT * FROM events ORDER BY event_date ASC, event_id DESC";
        List<Event> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load events: " + e.getMessage(), e);
        }
    }

    public List<Event> searchEvents(String keyword, String statusFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM events WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(event_name) LIKE ? OR LOWER(category) LIKE ? OR LOWER(venue) LIKE ? OR LOWER(organizer) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (statusFilter != null && !statusFilter.isBlank() && !"All".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND status = ?");
            params.add(statusFilter);
        }

        sql.append(" ORDER BY event_date ASC, event_id DESC");

        List<Event> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search events: " + e.getMessage(), e);
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM events";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count events: " + e.getMessage(), e);
        }
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM events WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count events by status: " + e.getMessage(), e);
        }
    }

    private void bindEventParameters(PreparedStatement ps, Event event) throws SQLException {
        ps.setString(1, event.getEventName());
        ps.setString(2, event.getDescription());
        ps.setString(3, event.getCategory());
        ps.setString(4, event.getDate());
        ps.setString(5, event.getStartTime());
        ps.setString(6, event.getEndTime());
        ps.setString(7, event.getVenue());
        ps.setString(8, event.getOrganizer());
        ps.setInt(9, event.getMaxParticipants());
        ps.setString(10, event.getStatus());
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("description"),
                rs.getString("category"),
                rs.getString("event_date"),
                rs.getString("start_time"),
                rs.getString("end_time"),
                rs.getString("venue"),
                rs.getString("organizer"),
                rs.getInt("max_participants"),
                rs.getString("status")
        );
    }
}
