package ca.gbc.comp3095.goaltrackingservice.service;

import ca.gbc.comp3095.goaltrackingservice.event.GoalCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "goal-completed-events";

    public void publishGoalCompletedEvent(GoalCompletedEvent event) {
        log.info("Publishing goal completed event for goal: {}", event.getGoalId());
        try {
            kafkaTemplate.send(TOPIC, event.getGoalId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully published event to topic: {} with offset: {}", 
                                TOPIC, result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to publish event to topic: {}", TOPIC, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing goal completed event", e);
        }
    }
}
