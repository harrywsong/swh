package ca.gbc.comp3095.eventservice.controller;

import ca.gbc.comp3095.eventservice.dto.EventRequest;
import ca.gbc.comp3095.eventservice.model.Event;
import ca.gbc.comp3095.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Event> getAllEvents() {
        return service.getAllEvents();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Event getEventById(@PathVariable Long id) {
        return service.getEventById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @GetMapping("/date/{date}")
    @ResponseStatus(HttpStatus.OK)
    public List<Event> getEventsByDate(@PathVariable LocalDateTime date) {
        return service.getEventsByDate(date);
    }

    @GetMapping("/location/{location}")
    @ResponseStatus(HttpStatus.OK)
    public List<Event> getEventsByLocation(@PathVariable String location) {
        return service.getEventsByLocation(location);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event createEvent(@RequestBody EventRequest request) {
        return service.createEvent(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Event updateEvent(@PathVariable Long id, @RequestBody EventRequest request) {
        return service.updateEvent(id, request);
    }

    @PostMapping("/{id}/register")
    @ResponseStatus(HttpStatus.OK)
    public Event registerStudent(@PathVariable Long id) {
        return service.registerStudent(id);
    }

    @PostMapping("/{id}/unregister")
    @ResponseStatus(HttpStatus.OK)
    public Event unregisterStudent(@PathVariable Long id) {
        return service.unregisterStudent(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        service.deleteEvent(id);
    }
}