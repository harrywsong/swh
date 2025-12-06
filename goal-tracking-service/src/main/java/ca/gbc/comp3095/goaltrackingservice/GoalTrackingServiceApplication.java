package ca.gbc.comp3095.goaltrackingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class GoalTrackingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoalTrackingServiceApplication.class, args);
    }

}
