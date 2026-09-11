package com.eventify.database;

import com.eventify.exception.DatabaseException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite JDBC connections to data/eventify.db with foreign keys enabled.
 */
public class DatabaseConnection {

    private static final String DB_DIR = "data";
    private static final String DB_FILE = "data/eventify.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        try {
            File dir = new File(DB_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new DatabaseException("Failed to create database directory: " + DB_DIR);
            }
            Connection connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException("Unable to connect to SQLite database: " + e.getMessage(), e);
        }
    }
}
