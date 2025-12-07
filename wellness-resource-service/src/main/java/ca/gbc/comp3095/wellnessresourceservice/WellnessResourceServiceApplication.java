package ca.gbc.comp3095.wellnessresourceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WellnessResourceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WellnessResourceServiceApplication.class, args);
    }

}
