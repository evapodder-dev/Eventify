package com.eventify.model;

/**
 * Enum representing task priority levels with associated urgency weights.
 * Demonstrates: Enums with constructors, fields, and behavior methods.
 */
public enum TaskPriority {
    LOW("Low", 1, "#16a34a"),
    MEDIUM("Medium", 2, "#ea580c"),
    HIGH("High", 3, "#dc2626");

    private final String displayName;
    private final int weight;
    private final String color;

    TaskPriority(String displayName, int weight, String color) {
        this.displayName = displayName;
        this.weight = weight;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWeight() {
        return weight;
    }

    public String getColor() {
        return color;
    }

    public boolean isUrgent() {
        return this == HIGH;
    }

    public static TaskPriority fromString(String text) {
        for (TaskPriority p : values()) {
            if (p.displayName.equalsIgnoreCase(text)) {
                return p;
            }
        }
        return MEDIUM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
