package ca.gbc.comp3095.goaltrackingservice.service;

import ca.gbc.comp3095.goaltrackingservice.dto.GoalTrackingRequest;
import ca.gbc.comp3095.goaltrackingservice.model.GoalTracking;

import java.util.List;
import java.util.Optional;

public interface GoalTrackingService {

    List<GoalTracking> getAllGoals();

    Optional<GoalTracking> getGoalById(String id);

    List<GoalTracking> getGoalsByCategory(String category);

    List<GoalTracking> getGoalsByStatus(String status);

    GoalTracking createGoal(GoalTrackingRequest request);

    GoalTracking updateGoal(String id, GoalTrackingRequest request);

    GoalTracking markGoalAsCompleted(String id);

    void deleteGoal(String id);
}