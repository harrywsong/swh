package ca.gbc.comp3095.wellnessresourceservice.listener;

import ca.gbc.comp3095.wellnessresourceservice.event.GoalCompletedEvent;
import ca.gbc.comp3095.wellnessresourceservice.model.ResourcePopularityTracker;
import ca.gbc.comp3095.wellnessresourceservice.repository.ResourcePopularityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class GoalCompletedEventListener {

    private final ResourcePopularityRepository popularityRepository;

    @KafkaListener(
            topics = "goal-completed-events",
            groupId = "wellness-resource-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGoalCompletedEvent(GoalCompletedEvent event) {
        log.info("Received goal completed event for tracking resource popularity. Goal: {} in category: {}", 
            event.getGoalId(), event.getCategory());

        try {
            trackResourcePopularity(event.getCategory());
            log.info("Successfully tracked popularity for category: {}", event.getCategory());
        } catch (Exception e) {
            log.error("Error tracking resource popularity for goal completed event", e);
        }
    }

    private void trackResourcePopularity(String category) {
        ResourcePopularityTracker tracker = popularityRepository
                .findByCategory(category)
                .orElse(ResourcePopularityTracker.builder()
                        .category(category)
                        .viewCount(0)
                        .goalCompletionCount(0)
                        .build());
        
        tracker.setGoalCompletionCount(tracker.getGoalCompletionCount() + 1);
        tracker.setLastUpdated(LocalDateTime.now());
        
        popularityRepository.save(tracker);
        
        log.info("Updated popularity tracker for category: {}. Total completions: {}", 
            category, tracker.getGoalCompletionCount());
    }
}
