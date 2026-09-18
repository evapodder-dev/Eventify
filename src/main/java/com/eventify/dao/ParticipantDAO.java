package com.eventify.dao;

import com.eventify.database.DatabaseConnection;
import com.eventify.exception.DatabaseException;
import com.eventify.model.Participant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for managing university participants in SQLite.
 */
public class ParticipantDAO implements CrudDAO<Participant, Integer> {

    @Override
    public boolean insert(Participant participant) {
        String sql = "INSERT INTO participants (name, student_id, email, phone, department, year) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, participant.getName());
            ps.setString(2, participant.getStudentId());
            ps.setString(3, participant.getEmail());
            ps.setString(4, participant.getPhone());
            ps.setString(5, participant.getDepartment());
            ps.setString(6, participant.getYear());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add participant: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Participant participant) {
        String sql = """
            UPDATE participants
            SET name = ?, student_id = ?, email = ?, phone = ?, department = ?, year = ?
            WHERE participant_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, participant.getName());
            ps.setString(2, participant.getStudentId());
            ps.setString(3, participant.getEmail());
            ps.setString(4, participant.getPhone());
            ps.setString(5, participant.getDepartment());
            ps.setString(6, participant.getYear());
            ps.setInt(7, participant.getParticipantId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update participant: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM participants WHERE participant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete participant: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Participant> findById(Integer id) {
        String sql = "SELECT * FROM participants WHERE participant_id = ?";
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
            throw new DatabaseException("Failed to find participant: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Participant> findAll() {
        String sql = "SELECT * FROM participants ORDER BY participant_id DESC";
        List<Participant> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load participants: " + e.getMessage(), e);
        }
    }

    public List<Participant> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String sql = """
            SELECT * FROM participants
            WHERE LOWER(name) LIKE ? OR LOWER(student_id) LIKE ? OR LOWER(department) LIKE ? OR LOWER(email) LIKE ?
            ORDER BY participant_id DESC
            """;
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        List<Participant> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search participants: " + e.getMessage(), e);
        }
    }

    private Participant mapRow(ResultSet rs) throws SQLException {
        return new Participant(
                rs.getInt("participant_id"),
                rs.getString("name"),
                rs.getString("student_id"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("department"),
                rs.getString("year")
        );
    }
}
