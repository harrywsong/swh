package ca.gbc.comp3095.goaltrackingservice.controller;

import ca.gbc.comp3095.goaltrackingservice.dto.GoalTrackingRequest;
import ca.gbc.comp3095.goaltrackingservice.model.GoalTracking;
import ca.gbc.comp3095.goaltrackingservice.service.GoalTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Tag(name = "Goal Tracking", description = "APIs for managing personal wellness goals")
public class GoalTrackingController {

    private final GoalTrackingService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all goals",
            description = "Retrieves a list of all wellness goals in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of goals",
                    content = @Content(schema = @Schema(implementation = GoalTracking.class)))
    })
    public List<GoalTracking> getAllGoals() {
        return service.getAllGoals();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get goal by ID",
            description = "Retrieves a specific wellness goal by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goal found",
                    content = @Content(schema = @Schema(implementation = GoalTracking.class))),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public GoalTracking getGoalById(
            @Parameter(description = "Goal ID", required = true) @PathVariable String id) {
        return service.getGoalById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));
    }

    @GetMapping("/category/{category}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get goals by category",
            description = "Retrieves all wellness goals filtered by a specific category."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved goals",
                    content = @Content(schema = @Schema(implementation = GoalTracking.class)))
    })
    public List<GoalTracking> getGoalsByCategory(
            @Parameter(description = "Category name", required = true) @PathVariable String category) {
        return service.getGoalsByCategory(category);
    }

    @GetMapping("/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get goals by status",
            description = "Retrieves all wellness goals filtered by status (e.g., IN_PROGRESS, COMPLETED, PENDING)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved goals",
                    content = @Content(schema = @Schema(implementation = GoalTracking.class)))
    })
    public List<GoalTracking> getGoalsByStatus(
            @Parameter(description = "Goal status", required = true) @PathVariable String status) {
        return service.getGoalsByStatus(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new goal",
            description = "Creates a new wellness goal. Requires student role. When a goal is completed, " +
                    "a GoalCompletedEvent is published to Kafka.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Goal created successfully",
                    content = @Content(schema = @Schema(implementation = GoalTracking.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Student role required")
    })
    public GoalTracking createGoal(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Goal details", required = true,
                    content = @Content(schema = @Schema(implementation = GoalTrackingRequest.class)))
            @RequestBody GoalTrackingRequest request) {
        return service.createGoal(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update a goal",
            description = "Updates an existing wellness goal.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goal updated successfully",
                    content = @Content(schema = @Schema(implementation = GoalTracking.class))),
            @ApiResponse(responseCode = "404", description = "Goal not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public GoalTracking updateGoal(
            @Parameter(description = "Goal ID", required = true) @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated goal details", required = true,
                    content = @Content(schema = @Schema(implementation = GoalTrackingRequest.class)))
            @RequestBody GoalTrackingRequest request) {
        return service.updateGoal(id, request);
    }

    @PatchMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Mark goal as completed",
            description = "Marks a goal as completed. This triggers a GoalCompletedEvent to be published to Kafka, " +
                    "which notifies other services for event recommendations.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goal marked as completed",
                    content = @Content(schema = @Schema(implementation = GoalTracking.class))),
            @ApiResponse(responseCode = "404", description = "Goal not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public GoalTracking markGoalAsCompleted(
            @Parameter(description = "Goal ID", required = true) @PathVariable String id) {
        return service.markGoalAsCompleted(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a goal",
            description = "Deletes a wellness goal by ID.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Goal deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Goal not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public void deleteGoal(
            @Parameter(description = "Goal ID", required = true) @PathVariable String id) {
        service.deleteGoal(id);
    }

    @GetMapping("/{id}/suggested-resources")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get suggested resources URL for a goal",
            description = "Returns a URL to fetch wellness resources that match the goal's category."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public String getSuggestedResourcesUrl(
            @Parameter(description = "Goal ID", required = true) @PathVariable String id) {
        GoalTracking goal = service.getGoalById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));

        // Return URL to fetch wellness resources matching this goal's category
        return "http://wellness-resource-service:8081/api/resources/category/" + goal.getCategory();
    }
}