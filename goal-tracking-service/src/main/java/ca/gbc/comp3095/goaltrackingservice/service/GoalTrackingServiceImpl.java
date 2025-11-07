package ca.gbc.comp3095.goaltrackingservice.service;

import ca.gbc.comp3095.goaltrackingservice.dto.GoalTrackingRequest;
import ca.gbc.comp3095.goaltrackingservice.model.GoalTracking;
import ca.gbc.comp3095.goaltrackingservice.repository.GoalTrackingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GoalTrackingServiceImpl implements GoalTrackingService {

    private final GoalTrackingRepository repository;

    @Override
    public List<GoalTracking> getAllGoals() {
        log.info("Fetching all goals");
        return repository.findAll();
    }

    @Override
    public Optional<GoalTracking> getGoalById(String id) {
        log.info("Fetching goal with id: {}", id);
        return repository.findById(id);
    }

    @Override
    public List<GoalTracking> getGoalsByCategory(String category) {
        log.info("Fetching goals for category: {}", category);
        return repository.findAll().stream()
                .filter(goal -> goal.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    @Override
    public List<GoalTracking> getGoalsByStatus(String status) {
        log.info("Fetching goals with status: {}", status);
        return repository.findAll().stream()
                .filter(goal -> goal.getStatus().equalsIgnoreCase(status))
                .toList();
    }

    @Override
    public GoalTracking createGoal(GoalTrackingRequest request) {
        log.info("Creating new goal: {}", request.title());

        GoalTracking goal = GoalTracking.builder()
                .title(request.title())
                .description(request.description())
                .targetDate(request.targetDate())
                .status(request.status() != null ? request.status() : "in-progress")
                .category(request.category())
                .build();

        return repository.save(goal);
    }

    @Override
    public GoalTracking updateGoal(String id, GoalTrackingRequest request) {
        log.info("Updating goal with id: {}", id);
        GoalTracking goal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));

        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setTargetDate(request.targetDate());
        goal.setStatus(request.status());
        goal.setCategory(request.category());

        return repository.save(goal);
    }

    @Override
    public GoalTracking markGoalAsCompleted(String id) {
        log.info("Marking goal as completed: {}", id);
        GoalTracking goal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));

        goal.setStatus("completed");
        return repository.save(goal);
    }

    @Override
    public void deleteGoal(String id) {
        log.info("Deleting goal with id: {}", id);
        repository.deleteById(id);
    }
}