package com.eventify.model;

/**
 * Represents an Event Organizer with full management privileges.
 * Demonstrates inheritance and method overriding.
 */
public class Organizer extends User {

    public Organizer() {
        super();
        setRole("Organizer");
    }

    public Organizer(int userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email, "Organizer");
    }

    @Override
    public String getAccessSummary() {
        return "Organizer Access — Full Event, Participant, Attendance, Schedule, Task & Leaderboard Control";
    }

    @Override
    public boolean canManageSystem() {
        return true;
    }
}
