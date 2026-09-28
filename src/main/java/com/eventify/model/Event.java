package com.eventify.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a University Event in Eventify.
 * Demonstrates: Implementing multiple interfaces (Searchable, Exportable),
 * Comparable interface for natural ordering, and Enum usage.
 */
public class Event implements Searchable, Exportable, Comparable<Event> {

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

    // --- Searchable Interface Implementation ---
    @Override
    public boolean matchesKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String lower = keyword.toLowerCase();
        return (eventName != null && eventName.toLowerCase().contains(lower))
                || (category != null && category.toLowerCase().contains(lower))
                || (venue != null && venue.toLowerCase().contains(lower))
                || (organizer != null && organizer.toLowerCase().contains(lower));
    }

    @Override
    public String getSearchLabel() {
        return "#" + eventId + " " + eventName + " [" + category + "]";
    }

    // --- Exportable Interface Implementation ---
    @Override
    public Map<String, Object> toExportMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("eventName", eventName);
        map.put("category", category);
        map.put("date", date);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("venue", venue);
        map.put("organizer", organizer);
        map.put("maxParticipants", maxParticipants);
        map.put("status", status);
        map.put("description", description);
        return map;
    }

    @Override
    public String toExportSummary() {
        return String.format("%s | %s | %s | %s | %s", eventName, category, date, venue, status);
    }

    // --- Comparable Interface Implementation ---
    @Override
    public int compareTo(Event other) {
        if (this.date == null && other.date == null) return 0;
        if (this.date == null) return 1;
        if (other.date == null) return -1;
        return this.date.compareTo(other.date);
    }

    // --- Enum convenience ---
    public EventStatus getEventStatus() {
        return EventStatus.fromString(status);
    }

    // --- Getters and Setters ---
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
        return eventName;
    }
}
