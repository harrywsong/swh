package ca.gbc.comp3095.goaltrackingservice.repository;

import ca.gbc.comp3095.goaltrackingservice.model.GoalTracking;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoalTrackingRepository extends MongoRepository<GoalTracking, String> {
}