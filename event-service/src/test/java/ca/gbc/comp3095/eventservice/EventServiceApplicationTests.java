package ca.gbc.comp3095.eventservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EventServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
