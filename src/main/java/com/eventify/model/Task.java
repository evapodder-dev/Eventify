package com.eventify.model;

/**
 * Represents a coordination Task assigned for a university Event.
 */
public class Task {

    private int taskId;
    private int eventId;
    private String eventName;
    private String taskName;
    private String assignedTo;
    private String deadline;
    private String priority;
    private String status;

    public Task() {
    }

    public Task(int taskId, int eventId, String eventName, String taskName,
                String assignedTo, String deadline, String priority, String status) {
        this.taskId = taskId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.taskName = taskName;
        this.assignedTo = assignedTo;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
