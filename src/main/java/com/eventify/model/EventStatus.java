package com.eventify.model;

/**
 * Enum representing the possible statuses of a University Event.
 * Demonstrates: Java Enums with fields, constructor, and methods.
 */
public enum EventStatus {
    UPCOMING("Upcoming", "#0284c7"),
    ONGOING("Ongoing", "#16a34a"),
    COMPLETED("Completed", "#64748b");

    private final String displayName;
    private final String color;

    EventStatus(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public static EventStatus fromString(String text) {
        for (EventStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(text)) {
                return status;
            }
        }
        return UPCOMING;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
