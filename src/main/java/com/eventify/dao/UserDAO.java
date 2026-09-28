package com.eventify.dao;

import com.eventify.database.DatabaseConnection;
import com.eventify.exception.DatabaseException;
import com.eventify.model.Organizer;
import com.eventify.model.Participant;
import com.eventify.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Data Access Object for authenticating (Sign In) and registering (Sign Up) users in SQLite.
 */
public class UserDAO {

    /**
     * Verifies username/roll and password against the SQLite users table.
     * Returns a polymorphic User instance (Organizer or Participant).
     */
    public Optional<User> authenticate(String username, String password) {
        String trimmedUser = username != null ? username.trim() : "";
        String sql = "SELECT user_id, username, password, full_name, email, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trimmedUser);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String uname = rs.getString("username");
                    String pwd = rs.getString("password");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    String role = rs.getString("role");

                    if ("Organizer".equalsIgnoreCase(role)) {
                        return Optional.of(new Organizer(id, uname, pwd, fullName, email));
                    } else {
                        return Optional.of(new Participant(id, uname, pwd, uname, email));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Database error during login authentication: " + e.getMessage(), e);
        }
    }

    /**
     * Registers a new user account (Sign Up) in SQLite.
     * If the role is Participant, also ensures a corresponding participant record exists by Roll.
     */
    public User registerUser(String username, String password, String role) {
        String trimmedUser = username != null ? username.trim() : "";
        String normalizedRole = "Organizer".equalsIgnoreCase(role) ? "Organizer" : "Participant";
        String email = trimmedUser + "@student.university.edu";

        String checkSql = "SELECT user_id FROM users WHERE username = ?";
        String insertUserSql = "INSERT INTO users (username, password, full_name, email, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, trimmedUser);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        throw new DatabaseException("ID / Roll '" + trimmedUser + "' is already registered. Please click Sign In.");
                    }
                }
            }

            int newUserId = 0;
            try (PreparedStatement insertPs = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                insertPs.setString(1, trimmedUser);
                insertPs.setString(2, password);
                insertPs.setString(3, trimmedUser);
                insertPs.setString(4, email);
                insertPs.setString(5, normalizedRole);
                insertPs.executeUpdate();
                try (ResultSet keys = insertPs.getGeneratedKeys()) {
                    if (keys.next()) {
                        newUserId = keys.getInt(1);
                    }
                }
            }

            if ("Participant".equalsIgnoreCase(normalizedRole)) {
                String insertParticipantSql = """
                    INSERT OR IGNORE INTO participants (name, student_id, email, phone, department, year)
                    VALUES (?, ?, ?, '01700000000', 'CSE', '3rd Year')
                    """;
                try (PreparedStatement partPs = conn.prepareStatement(insertParticipantSql)) {
                    partPs.setString(1, "Participant (" + trimmedUser + ")");
                    partPs.setString(2, trimmedUser);
                    partPs.setString(3, email);
                    partPs.executeUpdate();
                }
                return new Participant(newUserId, trimmedUser, password, trimmedUser, email);
            } else {
                return new Organizer(newUserId, trimmedUser, password, trimmedUser, email);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to sign up user: " + e.getMessage(), e);
        }
    }
}
