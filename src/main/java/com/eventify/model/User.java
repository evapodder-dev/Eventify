package com.eventify.model;

/**
 * Abstract base class representing an authenticated Eventify system user.
 * Demonstrates: Abstract classes, abstract methods, encapsulation, constructors,
 * polymorphism (subclass override), and the Template Method pattern.
 */
public abstract class User {

    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;

    protected User() {
    }

    protected User(int userId, String username, String password, String fullName, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    /**
     * Abstract method — each subclass must define its own access summary.
     * This is the key demonstration of abstract methods for APL.
     */
    public abstract String getAccessSummary();

    /**
     * Abstract method — determines if this user type can manage the system.
     */
    public abstract boolean canManageSystem();

    /**
     * Template method pattern: returns a formatted display string.
     * Subclasses customize via getAccessSummary() and canManageSystem().
     */
    public final String getDisplayInfo() {
        return String.format("[%s] %s — %s (Admin: %s)",
                role, fullName, getAccessSummary(), canManageSystem() ? "Yes" : "No");
    }

    // --- Getters and Setters (Encapsulation) ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
