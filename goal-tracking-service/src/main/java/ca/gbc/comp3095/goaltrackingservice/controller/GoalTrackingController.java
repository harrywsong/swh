package ca.gbc.comp3095.goaltrackingservice.controller;

import ca.gbc.comp3095.goaltrackingservice.dto.GoalTrackingRequest;
import ca.gbc.comp3095.goaltrackingservice.model.GoalTracking;
import ca.gbc.comp3095.goaltrackingservice.service.GoalTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalTrackingController {

    private final GoalTrackingService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GoalTracking> getAllGoals() {
        return service.getAllGoals();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GoalTracking getGoalById(@PathVariable String id) {
        return service.getGoalById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));
    }

    @GetMapping("/category/{category}")
    @ResponseStatus(HttpStatus.OK)
    public List<GoalTracking> getGoalsByCategory(@PathVariable String category) {
        return service.getGoalsByCategory(category);
    }

    @GetMapping("/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    public List<GoalTracking> getGoalsByStatus(@PathVariable String status) {
        return service.getGoalsByStatus(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalTracking createGoal(@RequestBody GoalTrackingRequest request) {
        return service.createGoal(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GoalTracking updateGoal(@PathVariable String id, @RequestBody GoalTrackingRequest request) {
        return service.updateGoal(id, request);
    }

    @PatchMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    public GoalTracking markGoalAsCompleted(@PathVariable String id) {
        return service.markGoalAsCompleted(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoal(@PathVariable String id) {
        service.deleteGoal(id);
    }

    @GetMapping("/{id}/suggested-resources")
    @ResponseStatus(HttpStatus.OK)
    public String getSuggestedResourcesUrl(@PathVariable String id) {
        GoalTracking goal = service.getGoalById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));

        // Return URL to fetch wellness resources matching this goal's category
        return "http://wellness-resource-service:8081/api/resources/category/" + goal.getCategory();
    }
}