package ca.gbc.comp3095.goaltrackingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic goalCompletedTopic() {
        return TopicBuilder.name("goal-completed-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
