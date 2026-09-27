package com.eventify.service;

import com.eventify.dao.CrudDAO;
import com.eventify.dao.EventDAO;
import com.eventify.model.Event;

import java.util.List;

/**
 * Service layer for validating and managing University Events.
 * Extends the abstract BaseService to inherit template CRUD methods,
 * and overrides the validate method with Event-specific business rules.
 */
public class EventService extends BaseService<Event, Integer> {

    private final EventDAO eventDAO = new EventDAO();

    @Override
    protected CrudDAO<Event, Integer> getDao() {
        return eventDAO;
    }

    @Override
    protected void validate(Event event) {
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

    // Keep backward-compatible methods used by controllers
    public void validateEvent(Event event) {
        validate(event);
    }

    public boolean createEvent(Event event) {
        return create(event);
    }

    public boolean updateEvent(Event event) {
        if (event.getEventId() <= 0) {
            throw new IllegalArgumentException("Please select an existing event to update.");
        }
        return update(event);
    }

    public boolean deleteEvent(int eventId) {
        return delete(eventId);
    }

    public List<Event> getAllEvents() {
        return getAll();
    }

    public List<Event> searchEvents(String keyword, String status) {
        return eventDAO.searchEvents(keyword, status);
    }
}
