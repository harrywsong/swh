package ca.gbc.comp3095.wellnessresourceservice;

import ca.gbc.comp3095.wellnessresourceservice.event.GoalCompletedEvent;
import ca.gbc.comp3095.wellnessresourceservice.listener.GoalCompletedEventListener;
import ca.gbc.comp3095.wellnessresourceservice.model.ResourcePopularityTracker;
import ca.gbc.comp3095.wellnessresourceservice.repository.ResourcePopularityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = WellnessResourceServiceApplication.class)
@DirtiesContext
@Testcontainers
@Import(KafkaIntegrationTest.TestKafkaProducerConfig.class)
class KafkaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"));

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Configuration
    static class TestKafkaProducerConfig {
        @Bean
        public ProducerFactory<String, Object> producerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
            Map<String, Object> config = new HashMap<>();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
            return new DefaultKafkaProducerFactory<>(config);
        }

        @Bean
        public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }
    }

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

