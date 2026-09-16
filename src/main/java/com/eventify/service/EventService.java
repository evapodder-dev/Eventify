package com.eventify.service;

import com.eventify.dao.EventDAO;
import com.eventify.model.Event;

import java.util.List;

/**
 * Service layer for validating and managing University Events.
 */
public class EventService {

    private final EventDAO eventDAO = new EventDAO();

    public void validateEvent(Event event) {
        if (event.getEventName() == null || event.getEventName().isBlank()) {
            throw new IllegalArgumentException("Event Name is required.");
        }
        if (event.getCategory() == null || event.getCategory().isBlank()) {
            throw new IllegalArgumentException("Event Category is required.");
        }
        if (event.getDate() == null || event.getDate().isBlank()) {
            throw new IllegalArgumentException("Event Date is required.");
        }
        if (event.getStartTime() == null || event.getStartTime().isBlank()) {
            throw new IllegalArgumentException("Start Time is required.");
        }
        if (event.getEndTime() == null || event.getEndTime().isBlank()) {
            throw new IllegalArgumentException("End Time is required.");
        }
        if (event.getVenue() == null || event.getVenue().isBlank()) {
            throw new IllegalArgumentException("Venue is required.");
        }
        if (event.getOrganizer() == null || event.getOrganizer().isBlank()) {
            throw new IllegalArgumentException("Organizer is required.");
        }
        if (event.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("Maximum Participants must be greater than 0.");
        }
        if (event.getStatus() == null || event.getStatus().isBlank()) {
            throw new IllegalArgumentException("Event Status is required.");
        }
    }

    public boolean createEvent(Event event) {
        validateEvent(event);
        return eventDAO.insert(event);
    }

    public boolean updateEvent(Event event) {
        if (event.getEventId() <= 0) {
            throw new IllegalArgumentException("Please select an existing event to update.");
        }
        validateEvent(event);
        return eventDAO.update(event);
    }

    public boolean deleteEvent(int eventId) {
        return eventDAO.delete(eventId);
    }

    public List<Event> getAllEvents() {
        return eventDAO.findAll();
    }

    public List<Event> searchEvents(String keyword, String status) {
        return eventDAO.searchEvents(keyword, status);
    }
}
