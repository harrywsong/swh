package ca.gbc.comp3095.eventservice.listener;

import ca.gbc.comp3095.eventservice.event.GoalCompletedEvent;
import ca.gbc.comp3095.eventservice.model.Event;
import ca.gbc.comp3095.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class GoalCompletedEventListener {

    private final EventRepository eventRepository;

    @KafkaListener(
            topics = "goal-completed-events",
            groupId = "event-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGoalCompletedEvent(GoalCompletedEvent event) {
        log.info("Received goal completed event for goal: {} in category: {}", 
            event.getGoalId(), event.getCategory());

        try {
            List<Event> relevantEvents = findRelevantEvents(event.getCategory());
            
            if (!relevantEvents.isEmpty()) {
                log.info("Found {} relevant events for category: {}", 
                    relevantEvents.size(), event.getCategory());
                
                relevantEvents.forEach(e -> 
                    log.info("Recommended event: {} at {}", e.getTitle(), e.getLocation())
                );
            } else {
                log.info("No relevant events found for category: {}", event.getCategory());
            }
            
        } catch (Exception e) {
            log.error("Error processing goal completed event", e);
        }
    }

    private List<Event> findRelevantEvents(String goalCategory) {
        return eventRepository.findAll().stream()
                .filter(event -> event.getTitle().toLowerCase().contains(goalCategory.toLowerCase()) 
                        || event.getDescription().toLowerCase().contains(goalCategory.toLowerCase()))
                .filter(event -> event.getDate().isAfter(LocalDateTime.now()))
                .filter(event -> event.getRegisteredStudents() < event.getCapacity())
                .limit(5)
                .toList();
    }
}
