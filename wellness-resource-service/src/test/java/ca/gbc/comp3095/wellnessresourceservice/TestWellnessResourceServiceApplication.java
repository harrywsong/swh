package ca.gbc.comp3095.wellnessresourceservice;

import org.springframework.boot.SpringApplication;

public class TestWellnessResourceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(WellnessResourceServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
