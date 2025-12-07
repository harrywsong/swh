package ca.gbc.comp3095.wellnessresourceservice;

import ca.gbc.comp3095.wellnessresourceservice.event.GoalCompletedEvent;
import ca.gbc.comp3095.wellnessresourceservice.listener.GoalCompletedEventListener;
import ca.gbc.comp3095.wellnessresourceservice.model.ResourcePopularityTracker;
import ca.gbc.comp3095.wellnessresourceservice.repository.ResourcePopularityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"goal-completed-events"})
@DirtiesContext
@ActiveProfiles("test")
class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ResourcePopularityRepository popularityRepository;

    @Autowired
    private GoalCompletedEventListener eventListener;

    @BeforeEach
    void setUp() {
        popularityRepository.deleteAll();
    }

    @Test
    void testConsumeGoalCompletedEventAndTrackPopularity() {
        // Send a GoalCompletedEvent to Kafka
        GoalCompletedEvent goalEvent = GoalCompletedEvent.builder()
                .goalId("goal-123")
                .title("Complete Meditation Goal")
                .category("mindfulness")
                .targetDate(LocalDate.now().plusMonths(1))
                .completedAt(LocalDateTime.now())
                .eventType("GOAL_COMPLETED")
                .build();

        // Send the event to Kafka
        kafkaTemplate.send("goal-completed-events", goalEvent.getGoalId(), goalEvent);

        // Wait for the listener to process the event and update popularity
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<ResourcePopularityTracker> tracker = popularityRepository.findByCategory("mindfulness");
            assertThat(tracker).isPresent();
            assertThat(tracker.get().getGoalCompletionCount()).isEqualTo(1);
            assertThat(tracker.get().getCategory()).isEqualTo("mindfulness");
        });
    }

    @Test
    void testMultipleGoalCompletionsIncrementPopularity() {
        String category = "fitness";

        // Send first goal completion event
        GoalCompletedEvent event1 = GoalCompletedEvent.builder()
                .goalId("goal-1")
                .title("Run 5K")
                .category(category)
                .targetDate(LocalDate.now().plusMonths(1))
                .completedAt(LocalDateTime.now())
                .eventType("GOAL_COMPLETED")
                .build();

        kafkaTemplate.send("goal-completed-events", event1.getGoalId(), event1);

        // Wait for first event to be processed
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<ResourcePopularityTracker> tracker = popularityRepository.findByCategory(category);
            assertThat(tracker).isPresent();
            assertThat(tracker.get().getGoalCompletionCount()).isEqualTo(1);
        });

        // Send second goal completion event
        GoalCompletedEvent event2 = GoalCompletedEvent.builder()
                .goalId("goal-2")
                .title("Lift Weights")
                .category(category)
                .targetDate(LocalDate.now().plusMonths(1))
                .completedAt(LocalDateTime.now())
                .eventType("GOAL_COMPLETED")
                .build();

        kafkaTemplate.send("goal-completed-events", event2.getGoalId(), event2);

        // Wait for second event to be processed
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<ResourcePopularityTracker> tracker = popularityRepository.findByCategory(category);
            assertThat(tracker).isPresent();
            assertThat(tracker.get().getGoalCompletionCount()).isEqualTo(2);
        });
    }
}

