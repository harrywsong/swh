package ca.gbc.comp3095.eventservice.service;

import ca.gbc.comp3095.eventservice.dto.EventRequest;
import ca.gbc.comp3095.eventservice.model.Event;
import ca.gbc.comp3095.eventservice.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository repository;

    @Override
    public List<Event> getAllEvents() {
        log.info("Fetching all events");
        return repository.findAll();
    }

    @Override
    public Optional<Event> getEventById(Long id) {
        log.info("Fetching event with id: {}", id);
        return repository.findById(id);
    }

    @Override
    public List<Event> getEventsByDate(LocalDateTime date) {
        log.info("Fetching events for date: {}", date);
        return repository.findAll().stream()
                .filter(event -> event.getDate().toLocalDate().equals(date.toLocalDate()))
                .toList();
    }

    @Override
    public List<Event> getEventsByLocation(String location) {
        log.info("Fetching events for location: {}", location);
        return repository.findAll().stream()
                .filter(event -> event.getLocation().equalsIgnoreCase(location))
                .toList();
    }

    @Override
    public Event createEvent(EventRequest request) {
        log.info("Creating new event: {}", request.title());

        Event event = Event.builder()
                .title(request.title())
                .description(request.description())
                .date(request.date())
                .location(request.location())
                .capacity(request.capacity())
                .registeredStudents(0)
                .build();

        return repository.save(event);
    }

    @Override
    public Event updateEvent(Long id, EventRequest request) {
        log.info("Updating event with id: {}", id);
        Event event = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setDate(request.date());
        event.setLocation(request.location());
        event.setCapacity(request.capacity());

        return repository.save(event);
    }

    @Override
    public Event registerStudent(Long eventId) {
        log.info("Registering student for event: {}", eventId);
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (event.getRegisteredStudents() >= event.getCapacity()) {
            throw new RuntimeException("Event is at full capacity");
        }

        event.setRegisteredStudents(event.getRegisteredStudents() + 1);
        return repository.save(event);
    }

    @Override
    public Event unregisterStudent(Long eventId) {
        log.info("Unregistering student from event: {}", eventId);
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (event.getRegisteredStudents() <= 0) {
            throw new RuntimeException("No students registered for this event");
        }

        event.setRegisteredStudents(event.getRegisteredStudents() - 1);
        return repository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        log.info("Deleting event with id: {}", id);
        repository.deleteById(id);
    }
}