package com.eventify.dao;

import com.eventify.database.DatabaseConnection;
import com.eventify.exception.DatabaseException;
import com.eventify.model.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for SQLite CRUD operations on the schedules table.
 */
public class ScheduleDAO implements CrudDAO<Schedule, Integer> {

    @Override
    public boolean insert(Schedule schedule) {
        String sql = """
            INSERT INTO schedules (event_id, activity_name, schedule_date, start_time, end_time, venue)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, schedule.getEventId());
            ps.setString(2, schedule.getActivityName());
            ps.setString(3, schedule.getDate());
            ps.setString(4, schedule.getStartTime());
            ps.setString(5, schedule.getEndTime());
            ps.setString(6, schedule.getVenue());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add schedule: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Schedule schedule) {
        String sql = """
            UPDATE schedules
            SET event_id = ?, activity_name = ?, schedule_date = ?, start_time = ?, end_time = ?, venue = ?
            WHERE schedule_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, schedule.getEventId());
            ps.setString(2, schedule.getActivityName());
            ps.setString(3, schedule.getDate());
            ps.setString(4, schedule.getStartTime());
            ps.setString(5, schedule.getEndTime());
            ps.setString(6, schedule.getVenue());
            ps.setInt(7, schedule.getScheduleId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update schedule: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM schedules WHERE schedule_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete schedule: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Schedule> findById(Integer id) {
        String sql = """
            SELECT s.*, e.event_name
            FROM schedules s
            JOIN events e ON s.event_id = e.event_id
            WHERE s.schedule_id = ?
            """;
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
            throw new DatabaseException("Failed to find schedule: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Schedule> findAll() {
        String sql = """
            SELECT s.*, e.event_name
            FROM schedules s
            JOIN events e ON s.event_id = e.event_id
            ORDER BY s.schedule_date ASC, s.start_time ASC
            """;
        List<Schedule> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load schedules: " + e.getMessage(), e);
        }
    }

    public List<Schedule> findByEventId(int eventId) {
        String sql = """
            SELECT s.*, e.event_name
            FROM schedules s
            JOIN events e ON s.event_id = e.event_id
            WHERE s.event_id = ?
            ORDER BY s.schedule_date ASC, s.start_time ASC
            """;
        List<Schedule> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to filter schedules: " + e.getMessage(), e);
        }
    }

    private Schedule mapRow(ResultSet rs) throws SQLException {
        return new Schedule(
                rs.getInt("schedule_id"),
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("activity_name"),
                rs.getString("schedule_date"),
                rs.getString("start_time"),
                rs.getString("end_time"),
                rs.getString("venue")
        );
    }
}
