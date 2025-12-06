package ca.gbc.comp3095.eventservice.controller;

import ca.gbc.comp3095.eventservice.client.GoalTrackingClient;
import ca.gbc.comp3095.eventservice.client.WellnessResourceClient;
import ca.gbc.comp3095.eventservice.dto.EventRequest;
import ca.gbc.comp3095.eventservice.dto.GoalTracking;
import ca.gbc.comp3095.eventservice.dto.WellnessResource;
import ca.gbc.comp3095.eventservice.model.Event;
import ca.gbc.comp3095.eventservice.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "APIs for managing wellness events")
public class EventController {

    private final EventService service;
    private final WellnessResourceClient wellnessResourceClient;
    private final GoalTrackingClient goalTrackingClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all events",
            description = "Retrieves a list of all available wellness events in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of events",
                    content = @Content(schema = @Schema(implementation = Event.class)))
    })
    public List<Event> getAllEvents() {
        return service.getAllEvents();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get event by ID",
            description = "Retrieves a specific wellness event by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event found",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public Event getEventById(
            @Parameter(description = "Event ID", required = true) @PathVariable Long id) {
        return service.getEventById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @GetMapping("/date/{date}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get events by date",
            description = "Retrieves all events scheduled for a specific date and time."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved events",
                    content = @Content(schema = @Schema(implementation = Event.class)))
    })
    public List<Event> getEventsByDate(
            @Parameter(description = "Event date and time", required = true)
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return service.getEventsByDate(date);
    }

    @GetMapping("/location/{location}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get events by location",
            description = "Retrieves all events at a specific location."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved events",
                    content = @Content(schema = @Schema(implementation = Event.class)))
    })
    public List<Event> getEventsByLocation(
            @Parameter(description = "Event location", required = true) @PathVariable String location) {
        return service.getEventsByLocation(location);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new event",
            description = "Creates a new wellness event.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public Event createEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Event details", required = true,
                    content = @Content(schema = @Schema(implementation = EventRequest.class)))
            @RequestBody EventRequest request) {
        return service.createEvent(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update an event",
            description = "Updates an existing wellness event.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public Event updateEvent(
            @Parameter(description = "Event ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated event details", required = true,
                    content = @Content(schema = @Schema(implementation = EventRequest.class)))
            @RequestBody EventRequest request) {
        return service.updateEvent(id, request);
    }

    @PostMapping("/{id}/register")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Register for an event",
            description = "Registers a student for a wellness event. Requires student role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered for event",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Student role required")
    })
    public Event registerStudent(
            @Parameter(description = "Event ID", required = true) @PathVariable Long id) {
        return service.registerStudent(id);
    }

    @PostMapping("/{id}/unregister")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Unregister from an event",
            description = "Unregisters a student from a wellness event. Requires student role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unregistered from event",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Student role required")
    })
    public Event unregisterStudent(
            @Parameter(description = "Event ID", required = true) @PathVariable Long id) {
        return service.unregisterStudent(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete an event",
            description = "Deletes a wellness event by ID.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public void deleteEvent(
            @Parameter(description = "Event ID", required = true) @PathVariable Long id) {
        service.deleteEvent(id);
    }

    @GetMapping("/{id}/related-resources")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get related resources for an event",
            description = "Returns wellness resources related to an event. Uses circuit breaker for fault tolerance."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resources retrieved successfully (may be empty if service is down)",
                    content = @Content(schema = @Schema(implementation = WellnessResource.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public List<WellnessResource> getRelatedResources(
            @Parameter(description = "Event ID", required = true) @PathVariable Long id) {
        Event event = service.getEventById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Call wellness-resource-service with circuit breaker protection
        return wellnessResourceClient.getAllResources();
    }

    @GetMapping("/{id}/related-goals")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get related goals for an event",
            description = "Returns goals that match the event category. Uses circuit breaker for fault tolerance."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goals retrieved successfully (may be empty if service is down)",
                    content = @Content(schema = @Schema(implementation = GoalTracking.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public List<GoalTracking> getRelatedGoals(
            @Parameter(description = "Event ID", required = true) @PathVariable Long id) {
        Event event = service.getEventById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Extract category from event (assuming event has a category field or derive from title/description)
        // For now, we'll use a default category or extract from event title
        String category = extractCategoryFromEvent(event);
        
        // Call goal-tracking-service with circuit breaker protection
        return goalTrackingClient.getGoalsByCategory(category);
    }

    private String extractCategoryFromEvent(Event event) {
        // Simple extraction logic - in a real scenario, events might have a category field
        // For now, we'll use a default or extract from title
        if (event.getTitle() != null && event.getTitle().toLowerCase().contains("fitness")) {
            return "Fitness";
        } else if (event.getTitle() != null && event.getTitle().toLowerCase().contains("mental")) {
            return "Mental Health";
        } else if (event.getTitle() != null && event.getTitle().toLowerCase().contains("nutrition")) {
            return "Nutrition";
        }
        return "General";
    }
}