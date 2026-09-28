package com.eventify.dao;

import com.eventify.database.DatabaseConnection;
import com.eventify.exception.DatabaseException;
import com.eventify.model.Registration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing Participant-to-Event Registrations and Attendance records in SQLite.
 */
public class RegistrationDAO {

    public boolean registerParticipant(int participantId, int eventId) {
        String sql = """
            INSERT INTO registrations (participant_id, event_id, registration_date, attendance_status)
            VALUES (?, ?, ?, 'Not Marked')
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ps.setInt(2, eventId);
            ps.setString(3, LocalDate.now().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new DatabaseException("Participant is already registered for this event.", e);
            }
            throw new DatabaseException("Failed to register participant: " + e.getMessage(), e);
        }
    }

    public boolean updateAttendance(int registrationId, int participantId, int eventId, String status) {
        String updateRegSql = "UPDATE registrations SET attendance_status = ? WHERE registration_id = ?";
        String upsertAttSql = """
            INSERT INTO attendance (registration_id, participant_id, event_id, status, updated_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(registration_id) DO UPDATE SET status = excluded.status, updated_at = excluded.updated_at
            """;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psReg = conn.prepareStatement(updateRegSql);
                 PreparedStatement psAtt = conn.prepareStatement(upsertAttSql)) {
                psReg.setString(1, status);
                psReg.setInt(2, registrationId);
                psReg.executeUpdate();

                psAtt.setInt(1, registrationId);
                psAtt.setInt(2, participantId);
                psAtt.setInt(3, eventId);
                psAtt.setString(4, status);
                psAtt.setString(5, timestamp);
                psAtt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update attendance: " + e.getMessage(), e);
        }
    }

    public boolean deleteRegistration(int registrationId) {
        String sql = "DELETE FROM registrations WHERE registration_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete registration: " + e.getMessage(), e);
        }
    }

    public List<Registration> findAll() {
        String sql = """
            SELECT r.registration_id, r.participant_id, p.name AS participant_name, p.student_id,
                   r.event_id, e.event_name, r.registration_date, r.attendance_status
            FROM registrations r
            JOIN participants p ON r.participant_id = p.participant_id
            JOIN events e ON r.event_id = e.event_id
            ORDER BY r.registration_id DESC
            """;
        List<Registration> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load registrations: " + e.getMessage(), e);
        }
    }

    public List<Registration> findByParticipant(int participantId) {
        String sql = """
            SELECT r.registration_id, r.participant_id, p.name AS participant_name, p.student_id,
                   r.event_id, e.event_name, r.registration_date, r.attendance_status
            FROM registrations r
            JOIN participants p ON r.participant_id = p.participant_id
            JOIN events e ON r.event_id = e.event_id
            WHERE r.participant_id = ?
            ORDER BY r.registration_id DESC
            """;
        List<Registration> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load participant registrations: " + e.getMessage(), e);
        }
    }

    public boolean registerByRoll(String roll, int eventId) {
        String trimmedRoll = roll != null ? roll.trim() : "";
        String ensurePartSql = """
            INSERT OR IGNORE INTO participants (name, student_id, email, phone, department, year)
            VALUES (?, ?, ?, '01700000000', 'CSE', '3rd Year')
            """;
        String findPartSql = "SELECT participant_id FROM participants WHERE student_id = ?";

        int participantId = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ensurePs = conn.prepareStatement(ensurePartSql)) {
                ensurePs.setString(1, "Participant (" + trimmedRoll + ")");
                ensurePs.setString(2, trimmedRoll);
                ensurePs.setString(3, trimmedRoll + "@student.university.edu");
                ensurePs.executeUpdate();
            }
            try (PreparedStatement findPs = conn.prepareStatement(findPartSql)) {
                findPs.setString(1, trimmedRoll);
                try (ResultSet rs = findPs.executeQuery()) {
                    if (rs.next()) {
                        participantId = rs.getInt("participant_id");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to prepare participant roll for registration: " + e.getMessage(), e);
        }

        if (participantId <= 0) {
            throw new DatabaseException("Could not find or create participant record for Roll: " + trimmedRoll);
        }
        return registerParticipant(participantId, eventId);
    }

    public List<Registration> findByRoll(String roll) {
        String trimmedRoll = roll != null ? roll.trim() : "";
        String sql = """
            SELECT r.registration_id, r.participant_id, p.name AS participant_name, p.student_id,
                   r.event_id, e.event_name, r.registration_date, r.attendance_status
            FROM registrations r
            JOIN participants p ON r.participant_id = p.participant_id
            JOIN events e ON r.event_id = e.event_id
            WHERE p.student_id = ?
            ORDER BY r.registration_id DESC
            """;
        List<Registration> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trimmedRoll);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load registrations for Roll " + trimmedRoll + ": " + e.getMessage(), e);
        }
    }

    private Registration mapRow(ResultSet rs) throws SQLException {
        return new Registration(
                rs.getInt("registration_id"),
                rs.getInt("participant_id"),
                rs.getString("participant_name"),
                rs.getString("student_id"),
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("registration_date"),
                rs.getString("attendance_status")
        );
    }
}
