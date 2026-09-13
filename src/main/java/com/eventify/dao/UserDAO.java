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
import java.util.Optional;

/**
 * Data Access Object for authenticating and retrieving users from SQLite.
 */
public class UserDAO {

    /**
     * Verifies username and password against the SQLite users table.
     * Returns a polymorphic User instance (Organizer or Participant).
     */
    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT user_id, username, password, full_name, email, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
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
                        return Optional.of(new Participant(id, uname, pwd, fullName, email));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Database error during login authentication: " + e.getMessage(), e);
        }
    }
}
