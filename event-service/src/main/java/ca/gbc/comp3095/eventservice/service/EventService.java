package ca.gbc.comp3095.eventservice.service;

import ca.gbc.comp3095.eventservice.dto.EventRequest;
import ca.gbc.comp3095.eventservice.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventService {

    List<Event> getAllEvents();

    Optional<Event> getEventById(Long id);

    List<Event> getEventsByDate(LocalDateTime date);

    List<Event> getEventsByLocation(String location);

    Event createEvent(EventRequest request);

    Event updateEvent(Long id, EventRequest request);

    Event registerStudent(Long eventId);

    Event unregisterStudent(Long eventId);

    void deleteEvent(Long id);
}