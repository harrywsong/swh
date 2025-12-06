package ca.gbc.comp3095.goaltrackingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI goalTrackingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Goal Tracking Service API")
                        .description("API for managing personal wellness goals in the Student Wellness Hub. " +
                                "Students can create, update, and track their wellness goals. When a goal is completed, " +
                                "an event is published to Kafka to notify other services for recommendations.")
                        .version("1.0.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/goals").description("Gateway Server (Local)"),
                        new Server().url("http://localhost:8082").description("Direct Service (Local)"),
                        new Server().url("http://goal-tracking-service:8082").description("Container Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from Keycloak authentication")));
    }
}

