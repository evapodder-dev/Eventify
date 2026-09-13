package com.eventify.model;

/**
 * Represents both a Participant user account and a University Event Participant entity.
 * Demonstrates inheritance, constructor overloading, and method overriding.
 */
public class Participant extends User {

    private int participantId;
    private String name;
    private String studentId;
    private String phone;
    private String department;
    private String year;

    public Participant() {
        super();
        setRole("Participant");
    }

    // Constructor for authenticated Participant User
    public Participant(int userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email, "Participant");
        this.name = fullName;
    }

    // Constructor for Participant Management CRUD
    public Participant(int participantId, String name, String studentId, String email, String phone, String department, String year) {
        super(0, studentId, "", name, email, "Participant");
        this.participantId = participantId;
        this.name = name;
        this.studentId = studentId;
        this.phone = phone;
        this.department = department;
        this.year = year;
    }

    @Override
    public String getAccessSummary() {
        return "Participant Access — View Events, Schedules, Leaderboards & Registrations";
    }

    @Override
    public boolean canManageSystem() {
        return false;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public String getName() {
        return name != null ? name : getFullName();
    }

    public void setName(String name) {
        this.name = name;
        setFullName(name);
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        if (studentId != null && !studentId.isBlank()) {
            return getName() + " [" + studentId + "]";
        }
        return super.toString();
    }
}
