package com.eventify.model;

/**
 * Represents an Event fetched from a remote REST API or JSON feed.
 */
public class ApiEvent {

    private int id;
    private String title;
    private String category;
    private String date;
    private String venue;
    private String organizer;
    private int maxParticipants;
    private String body;

    public ApiEvent() {
    }

    public ApiEvent(int id, String title, String category, String date,
                    String venue, String organizer, int maxParticipants, String body) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.date = date;
        this.venue = venue;
        this.organizer = organizer;
        this.maxParticipants = maxParticipants;
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category != null ? category : "Seminar";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date != null ? date : "2026-11-10";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVenue() {
        return venue != null ? venue : "University Main Hall";
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getOrganizer() {
        return organizer != null ? organizer : "University Academic Council";
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public int getMaxParticipants() {
        return maxParticipants > 0 ? maxParticipants : 100;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getBody() {
        return body != null ? body : "";
    }

    public void setBody(String body) {
        this.body = body;
    }
}
