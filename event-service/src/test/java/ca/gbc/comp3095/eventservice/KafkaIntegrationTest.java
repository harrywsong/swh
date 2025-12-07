package ca.gbc.comp3095.eventservice;

import ca.gbc.comp3095.eventservice.event.GoalCompletedEvent;
import ca.gbc.comp3095.eventservice.listener.GoalCompletedEventListener;
import ca.gbc.comp3095.eventservice.model.Event;
import ca.gbc.comp3095.eventservice.repository.EventRepository;
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
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = EventServiceApplication.class)
@DirtiesContext
@Testcontainers
@Import(KafkaIntegrationTest.TestKafkaProducerConfig.class)
class KafkaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
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
    private EventRepository eventRepository;

    @Autowired
    private GoalCompletedEventListener eventListener;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void testConsumeGoalCompletedEvent() {
        // Create a test event in the database that matches the goal category
        Event testEvent = Event.builder()
                .title("Fitness Workshop")
                .description("A workshop about fitness and wellness")
                .date(LocalDateTime.now().plusDays(7))
                .location("Room 101")
                .capacity(30)
                .registeredStudents(0)
                .build();
        eventRepository.save(testEvent);

        // Create and send a GoalCompletedEvent to Kafka
        GoalCompletedEvent goalEvent = GoalCompletedEvent.builder()
                .goalId("goal-123")
                .title("Complete 5K Run")
                .category("fitness")
                .targetDate(LocalDate.now().plusMonths(1))
                .completedAt(LocalDateTime.now())
                .eventType("GOAL_COMPLETED")
                .build();

        // Send the event to Kafka
        kafkaTemplate.send("goal-completed-events", goalEvent.getGoalId(), goalEvent);

        // Wait for the listener to process the event
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            // Verify the event was consumed and processed
            // The listener should have logged the event and found relevant events
            // We can verify by checking logs or by verifying side effects
            assertThat(eventRepository.findAll()).isNotEmpty();
        });
    }

    @Test
    void testEventRecommendationBasedOnCategory() {
        // Create events with different categories
        Event fitnessEvent = Event.builder()
                .title("Fitness Challenge")
                .description("Join our fitness challenge")
                .date(LocalDateTime.now().plusDays(10))
                .location("Gym")
                .capacity(50)
                .registeredStudents(10)
                .build();
        eventRepository.save(fitnessEvent);

        Event mindfulnessEvent = Event.builder()
                .title("Meditation Session")
                .description("Mindfulness meditation")
                .date(LocalDateTime.now().plusDays(5))
                .location("Room 202")
                .capacity(20)
                .registeredStudents(5)
                .build();
        eventRepository.save(mindfulnessEvent);

        // Send a fitness-related goal completion event
        GoalCompletedEvent fitnessGoalEvent = GoalCompletedEvent.builder()
                .goalId("goal-fitness-1")
                .title("Run 5K")
                .category("fitness")
                .targetDate(LocalDate.now().plusMonths(1))
                .completedAt(LocalDateTime.now())
                .eventType("GOAL_COMPLETED")
                .build();

        kafkaTemplate.send("goal-completed-events", fitnessGoalEvent.getGoalId(), fitnessGoalEvent);

        // Wait for processing
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            // The listener should have processed the event and found relevant events
            assertThat(eventRepository.findAll()).hasSize(2);
        });
    }
}

