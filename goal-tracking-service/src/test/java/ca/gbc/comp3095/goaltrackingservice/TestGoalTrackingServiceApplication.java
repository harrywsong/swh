package ca.gbc.comp3095.goaltrackingservice;

import org.springframework.boot.SpringApplication;

public class TestGoalTrackingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(GoalTrackingServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
