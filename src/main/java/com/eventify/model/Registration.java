package com.eventify.model;

/**
 * Represents the relationship between a Participant and an Event, including Attendance status.
 */
public class Registration {

    private int registrationId;
    private int participantId;
    private String participantName;
    private String studentId;
    private int eventId;
    private String eventName;
    private String registrationDate;
    private String attendanceStatus;

    public Registration() {
    }

    public Registration(int registrationId, int participantId, String participantName, String studentId,
                        int eventId, String eventName, String registrationDate, String attendanceStatus) {
        this.registrationId = registrationId;
        this.participantId = participantId;
        this.participantName = participantName;
        this.studentId = studentId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.registrationDate = registrationDate;
        this.attendanceStatus = attendanceStatus;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
}
