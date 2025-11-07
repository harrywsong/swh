package ca.gbc.comp3095.eventservice;

import org.springframework.boot.SpringApplication;

public class TestEventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(EventServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
