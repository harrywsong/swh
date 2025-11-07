package ca.gbc.comp3095.studentwellnesshub;

import org.springframework.boot.SpringApplication;

public class TestStudentWellnessHubApplication {

    public static void main(String[] args) {
        SpringApplication.from(StudentWellnessHubApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
