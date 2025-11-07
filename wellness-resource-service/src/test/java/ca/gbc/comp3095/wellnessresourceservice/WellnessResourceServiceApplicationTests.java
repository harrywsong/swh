package ca.gbc.comp3095.wellnessresourceservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WellnessResourceServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
