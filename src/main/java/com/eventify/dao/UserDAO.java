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
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
            }

            // If this Roll exists in participants table but doesn't have a users row yet, allow Sign In and create user row
            String checkUserExists = "SELECT 1 FROM users WHERE username = ?";
            boolean userExists = false;
            try (PreparedStatement checkPs = conn.prepareStatement(checkUserExists)) {
                checkPs.setString(1, trimmedUser);
                try (ResultSet rs = checkPs.executeQuery()) {
                    userExists = rs.next();
                }
            }

            if (!userExists) {
                String checkParticipantSql = "SELECT email FROM participants WHERE student_id = ?";
                try (PreparedStatement partPs = conn.prepareStatement(checkParticipantSql)) {
                    partPs.setString(1, trimmedUser);
                    try (ResultSet rs = partPs.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(registerUser(trimmedUser, password, "Participant"));
                        }
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
     * Avoids Statement.RETURN_GENERATED_KEYS because sqlite-jdbc 3.44 throws SQLFeatureNotSupportedException.
     * If the role is Participant, also ensures a corresponding participant record exists by Roll.
     */
    public User registerUser(String username, String password, String role) {
        String trimmedUser = username != null ? username.trim() : "";
        String normalizedRole = "Organizer".equalsIgnoreCase(role) ? "Organizer" : "Participant";
        String email = trimmedUser + "@student.university.edu";

        String upsertUserSql = """
            INSERT INTO users (username, password, full_name, email, role)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(username) DO UPDATE SET
                password = excluded.password,
                full_name = excluded.full_name,
                role = excluded.role
            """;
        String findIdSql = "SELECT user_id FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement insertPs = conn.prepareStatement(upsertUserSql)) {
                insertPs.setString(1, trimmedUser);
                insertPs.setString(2, password);
                insertPs.setString(3, trimmedUser);
                insertPs.setString(4, email);
                insertPs.setString(5, normalizedRole);
                insertPs.executeUpdate();
            }

            int newUserId = 0;
            try (PreparedStatement idPs = conn.prepareStatement(findIdSql)) {
                idPs.setString(1, trimmedUser);
                try (ResultSet rs = idPs.executeQuery()) {
                    if (rs.next()) {
                        newUserId = rs.getInt("user_id");
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
