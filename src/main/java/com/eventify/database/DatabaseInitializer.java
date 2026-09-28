package com.eventify.database;

import com.eventify.exception.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Automatically initializes all relational SQLite tables and seeds default demo data on startup.
 */
public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('Organizer', 'Participant'))
                );
                """);

            // 2. events table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS events (
                    event_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    event_name TEXT NOT NULL,
                    description TEXT,
                    category TEXT NOT NULL,
                    event_date TEXT NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    venue TEXT NOT NULL,
                    organizer TEXT NOT NULL,
                    max_participants INTEGER NOT NULL CHECK(max_participants > 0),
                    status TEXT NOT NULL CHECK(status IN ('Upcoming', 'Ongoing', 'Completed'))
                );
                """);

            // 3. participants table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS participants (
                    participant_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    student_id TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    department TEXT NOT NULL,
                    year TEXT NOT NULL
                );
                """);

            // 4. registrations table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS registrations (
                    registration_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    participant_id INTEGER NOT NULL,
                    event_id INTEGER NOT NULL,
                    registration_date TEXT NOT NULL,
                    attendance_status TEXT NOT NULL DEFAULT 'Not Marked'
                        CHECK(attendance_status IN ('Present', 'Absent', 'Not Marked')),
                    UNIQUE(participant_id, event_id),
                    FOREIGN KEY (participant_id) REFERENCES participants(participant_id) ON DELETE CASCADE,
                    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
                );
                """);

            // 5. attendance table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS attendance (
                    attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    registration_id INTEGER NOT NULL UNIQUE,
                    participant_id INTEGER NOT NULL,
                    event_id INTEGER NOT NULL,
                    status TEXT NOT NULL CHECK(status IN ('Present', 'Absent', 'Not Marked')),
                    updated_at TEXT NOT NULL,
                    FOREIGN KEY (registration_id) REFERENCES registrations(registration_id) ON DELETE CASCADE,
                    FOREIGN KEY (participant_id) REFERENCES participants(participant_id) ON DELETE CASCADE,
                    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
                );
                """);

            // 6. schedules table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS schedules (
                    schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    event_id INTEGER NOT NULL,
                    activity_name TEXT NOT NULL,
                    schedule_date TEXT NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    venue TEXT NOT NULL,
                    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
                );
                """);

            // 7. tasks table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    task_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    event_id INTEGER NOT NULL,
                    task_name TEXT NOT NULL,
                    assigned_to TEXT NOT NULL,
                    deadline TEXT NOT NULL,
                    priority TEXT NOT NULL CHECK(priority IN ('Low', 'Medium', 'High')),
                    status TEXT NOT NULL CHECK(status IN ('Pending', 'In Progress', 'Completed')),
                    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
                );
                """);

            // 8. results table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS results (
                    result_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    event_id INTEGER NOT NULL,
                    participant_id INTEGER NOT NULL,
                    score DOUBLE NOT NULL,
                    rank INTEGER NOT NULL,
                    UNIQUE(event_id, participant_id),
                    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
                    FOREIGN KEY (participant_id) REFERENCES participants(participant_id) ON DELETE CASCADE
                );
                """);

            // Apply schema migrations if necessary
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("PRAGMA table_info(events)")) {
                boolean hasDesc = false;
                while (rs.next()) {
                    if ("description".equals(rs.getString("name"))) {
                        hasDesc = true;
                        break;
                    }
                }
                if (!hasDesc) {
                    s.execute("ALTER TABLE events ADD COLUMN description TEXT DEFAULT ''");
                }
            }

            seedInitialData(conn);

            // Ensure Organizer name is set to EVA PODDER and Roll numbers use numeric format (e.g., 2307032)
            try (Statement s = conn.createStatement()) {
                s.execute("UPDATE users SET full_name = 'EVA PODDER', email = 'evapodder@university.edu', role = 'Organizer' WHERE LOWER(username) = 'admin'");
                s.execute("UPDATE users SET role = 'Participant', full_name = username WHERE LOWER(username) != 'admin'");
                s.execute("INSERT OR IGNORE INTO users (username, password, full_name, email, role) VALUES ('2307032', 'student123', '2307032', '2307032@student.university.edu', 'Participant')");
                s.execute("UPDATE events SET organizer = 'EVA PODDER' WHERE organizer IN ('CSE Computer Club', 'Department of CSE', 'IEEE Student Branch')");
                s.execute("UPDATE participants SET student_id = '2307032' WHERE student_id = 'CSE-2023-014'");
                s.execute("UPDATE participants SET student_id = '2307033' WHERE student_id = 'CSE-2023-028'");
                s.execute("UPDATE participants SET student_id = '2307034' WHERE student_id = 'EEE-2024-009'");
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) - COUNT(DISTINCT rank) FROM results")) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        new com.eventify.dao.ResultDAO().recalculateAllRanks(conn);
                    }
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize SQLite database schema: " + e.getMessage(), e);
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        // Seed default users if empty
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO users (username, password, full_name, email, role) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, "admin123");
                    ps.setString(3, "EVA PODDER");
                    ps.setString(4, "evapodder@university.edu");
                    ps.setString(5, "Organizer");
                    ps.executeUpdate();

                    ps.setString(1, "2307032");
                    ps.setString(2, "student123");
                    ps.setString(3, "2307032");
                    ps.setString(4, "2307032@student.university.edu");
                    ps.setString(5, "Participant");
                    ps.executeUpdate();
                }
            }
        }

        // Seed sample university events if empty
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM events")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String eventSql = """
                    INSERT INTO events (event_name, description, category, event_date, start_time, end_time, venue, organizer, max_participants, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                try (PreparedStatement ps = conn.prepareStatement(eventSql)) {
                    ps.setString(1, "Inter-University Programming Contest");
                    ps.setString(2, "Competitive programming contest covering algorithms, data structures, and graph theory.");
                    ps.setString(3, "Programming Contest");
                    ps.setString(4, "2026-10-15");
                    ps.setString(5, "09:00");
                    ps.setString(6, "14:00");
                    ps.setString(7, "CSE Lab Complex 301");
                    ps.setString(8, "EVA PODDER");
                    ps.setInt(9, 120);
                    ps.setString(10, "Upcoming");
                    ps.executeUpdate();

                    ps.setString(1, "JavaFX & Modern Desktop Architecture Workshop");
                    ps.setString(2, "Hands-on APL workshop on JavaFX, FXML, Concurrency, and SQLite integration.");
                    ps.setString(3, "Workshop");
                    ps.setString(4, "2026-09-27");
                    ps.setString(5, "10:00");
                    ps.setString(6, "13:00");
                    ps.setString(7, "Software Engineering Lab 204");
                    ps.setString(8, "EVA PODDER");
                    ps.setInt(9, 60);
                    ps.setString(10, "Ongoing");
                    ps.executeUpdate();

                    ps.setString(1, "AI & Cybersecurity Research Seminar");
                    ps.setString(2, "Keynote seminar featuring faculty and industry researchers on applied AI security.");
                    ps.setString(3, "Seminar");
                    ps.setString(4, "2026-09-10");
                    ps.setString(5, "15:00");
                    ps.setString(6, "17:30");
                    ps.setString(7, "Central Auditorium");
                    ps.setString(8, "EVA PODDER");
                    ps.setInt(9, 250);
                    ps.setString(10, "Completed");
                    ps.executeUpdate();
                }

                // Seed sample participants
                String partSql = """
                    INSERT INTO participants (name, student_id, email, phone, department, year)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
                try (PreparedStatement ps = conn.prepareStatement(partSql)) {
                    ps.setString(1, "Tanvir Ahmed");
                    ps.setString(2, "2307032");
                    ps.setString(3, "tanvir@student.university.edu");
                    ps.setString(4, "01711001122");
                    ps.setString(5, "CSE");
                    ps.setString(6, "3rd Year");
                    ps.executeUpdate();

                    ps.setString(1, "Nusrat Jahan");
                    ps.setString(2, "2307033");
                    ps.setString(3, "nusrat@student.university.edu");
                    ps.setString(4, "01819223344");
                    ps.setString(5, "CSE");
                    ps.setString(6, "3rd Year");
                    ps.executeUpdate();

                    ps.setString(1, "Rafiul Islam");
                    ps.setString(2, "2307034");
                    ps.setString(3, "rafiul@student.university.edu");
                    ps.setString(4, "01912556677");
                    ps.setString(5, "EEE");
                    ps.setString(6, "2nd Year");
                    ps.executeUpdate();
                }

                // Seed sample registrations & attendance
                try (Statement s = conn.createStatement()) {
                    s.execute("INSERT INTO registrations (participant_id, event_id, registration_date, attendance_status) VALUES (1, 1, '2026-09-20', 'Not Marked')");
                    s.execute("INSERT INTO registrations (participant_id, event_id, registration_date, attendance_status) VALUES (2, 1, '2026-09-21', 'Not Marked')");
                    s.execute("INSERT INTO registrations (participant_id, event_id, registration_date, attendance_status) VALUES (1, 2, '2026-09-22', 'Present')");
                    s.execute("INSERT INTO registrations (participant_id, event_id, registration_date, attendance_status) VALUES (3, 3, '2026-09-05', 'Present')");

                    s.execute("INSERT INTO attendance (registration_id, participant_id, event_id, status, updated_at) VALUES (3, 1, 2, 'Present', '2026-09-27 10:15')");
                    s.execute("INSERT INTO attendance (registration_id, participant_id, event_id, status, updated_at) VALUES (4, 3, 3, 'Present', '2026-09-10 15:10')");

                    // Seed schedules
                    s.execute("INSERT INTO schedules (event_id, activity_name, schedule_date, start_time, end_time, venue) VALUES (1, 'Mock Contest & Kit Distribution', '2026-10-14', '15:00', '17:00', 'CSE Lab Complex 301')");
                    s.execute("INSERT INTO schedules (event_id, activity_name, schedule_date, start_time, end_time, venue) VALUES (1, 'Main Programming Contest Round', '2026-10-15', '09:00', '14:00', 'CSE Lab Complex 301')");
                    s.execute("INSERT INTO schedules (event_id, activity_name, schedule_date, start_time, end_time, venue) VALUES (2, 'Hands-On FXML & Concurrency Session', '2026-09-27', '10:00', '13:00', 'Software Engineering Lab 204')");

                    // Seed tasks
                    s.execute("INSERT INTO tasks (event_id, task_name, assigned_to, deadline, priority, status) VALUES (1, 'Prepare PC2 / DOMjudge Contest Server', 'Lab Technical Team', '2026-10-12', 'High', 'Pending')");
                    s.execute("INSERT INTO tasks (event_id, task_name, assigned_to, deadline, priority, status) VALUES (1, 'Print Participant ID Badges', 'Volunteer Committee', '2026-10-13', 'Medium', 'In Progress')");
                    s.execute("INSERT INTO tasks (event_id, task_name, assigned_to, deadline, priority, status) VALUES (2, 'Distribute Lab Starter Repository', 'Course Instructor', '2026-09-27', 'High', 'Completed')");

                    // Seed results
                    s.execute("INSERT INTO results (event_id, participant_id, score, rank) VALUES (3, 1, 94.5, 1)");
                    s.execute("INSERT INTO results (event_id, participant_id, score, rank) VALUES (3, 2, 89.0, 2)");
                    s.execute("INSERT INTO results (event_id, participant_id, score, rank) VALUES (3, 3, 82.5, 3)");
                }
            }
        }
    }
}

