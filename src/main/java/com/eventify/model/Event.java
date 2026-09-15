package com.eventify.model;

/**
 * Represents a University Event in Eventify.
 */
public class Event {

    private int eventId;
    private String eventName;
    private String description;
    private String category;
    private String date;
    private String startTime;
    private String endTime;
    private String venue;
    private String organizer;
    private int maxParticipants;
    private String status;

    public Event() {
    }

    public Event(int eventId, String eventName, String description, String category,
                 String date, String startTime, String endTime, String venue,
                 String organizer, int maxParticipants, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.category = category;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.organizer = organizer;
        this.maxParticipants = maxParticipants;
        this.status = status;
    }

    public Event(String eventName, String description, String category,
                 String date, String startTime, String endTime, String venue,
                 String organizer, int maxParticipants, String status) {
        this(0, eventName, description, category, date, startTime, endTime, venue, organizer, maxParticipants, status);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return eventId > 0 ? ("#" + eventId + " - " + eventName) : eventName;
    }
}
