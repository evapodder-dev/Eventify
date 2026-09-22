package com.eventify.dao;

import com.eventify.database.DatabaseConnection;
import com.eventify.exception.DatabaseException;
import com.eventify.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for SQLite CRUD operations on the tasks table.
 */
public class TaskDAO implements CrudDAO<Task, Integer> {

    @Override
    public boolean insert(Task task) {
        String sql = """
            INSERT INTO tasks (event_id, task_name, assigned_to, deadline, priority, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, task.getEventId());
            ps.setString(2, task.getTaskName());
            ps.setString(3, task.getAssignedTo());
            ps.setString(4, task.getDeadline());
            ps.setString(5, task.getPriority());
            ps.setString(6, task.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add task: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Task task) {
        String sql = """
            UPDATE tasks
            SET event_id = ?, task_name = ?, assigned_to = ?, deadline = ?, priority = ?, status = ?
            WHERE task_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, task.getEventId());
            ps.setString(2, task.getTaskName());
            ps.setString(3, task.getAssignedTo());
            ps.setString(4, task.getDeadline());
            ps.setString(5, task.getPriority());
            ps.setString(6, task.getStatus());
            ps.setInt(7, task.getTaskId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update task: " + e.getMessage(), e);
        }
    }

    public boolean markCompleted(int taskId) {
        String sql = "UPDATE tasks SET status = 'Completed' WHERE task_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to complete task: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM tasks WHERE task_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete task: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Task> findById(Integer id) {
        String sql = """
            SELECT t.*, e.event_name
            FROM tasks t
            JOIN events e ON t.event_id = e.event_id
            WHERE t.task_id = ?
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
            throw new DatabaseException("Failed to find task: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Task> findAll() {
        String sql = """
            SELECT t.*, e.event_name
            FROM tasks t
            JOIN events e ON t.event_id = e.event_id
            ORDER BY t.deadline ASC, t.task_id DESC
            """;
        List<Task> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load tasks: " + e.getMessage(), e);
        }
    }

    public List<Task> filterTasks(String priority, String status) {
        StringBuilder sql = new StringBuilder("""
            SELECT t.*, e.event_name
            FROM tasks t
            JOIN events e ON t.event_id = e.event_id
            WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();

        if (priority != null && !priority.isBlank() && !"All".equalsIgnoreCase(priority)) {
            sql.append(" AND t.priority = ?");
            params.add(priority);
        }
        if (status != null && !status.isBlank() && !"All".equalsIgnoreCase(status)) {
            sql.append(" AND t.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY t.deadline ASC, t.task_id DESC");

        List<Task> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to filter tasks: " + e.getMessage(), e);
        }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("task_id"),
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("task_name"),
                rs.getString("assigned_to"),
                rs.getString("deadline"),
                rs.getString("priority"),
                rs.getString("status")
        );
    }
}
