package com.eventify;

import com.eventify.database.DatabaseInitializer;
import javafx.application.Application;

/**
 * Main entry point for Eventify — Event Management & Coordination System.
 * Separating main() from the Application subclass allows launching directly from
 * IntelliJ IDEA's Run button as well as via 'mvn javafx:run'.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && "--init-db-only".equals(args[0])) {
            DatabaseInitializer.initialize();
            System.out.println("SQLite database initialized successfully at data/eventify.db");
            return;
        }
        Application.launch(EventifyApp.class, args);
    }
}
