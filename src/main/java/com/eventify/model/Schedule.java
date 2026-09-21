package com.eventify.model;

/**
 * Represents an activity schedule entry for an Event.
 */
public class Schedule {

    private int scheduleId;
    private int eventId;
    private String eventName;
    private String activityName;
    private String date;
    private String startTime;
    private String endTime;
    private String venue;

    public Schedule() {
    }

    public Schedule(int scheduleId, int eventId, String eventName, String activityName,
                    String date, String startTime, String endTime, String venue) {
        this.scheduleId = scheduleId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.activityName = activityName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
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

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
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
}
