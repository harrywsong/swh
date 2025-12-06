package ca.gbc.comp3095.wellnessresourceservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
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
    public OpenAPI wellnessResourceServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wellness Resource Service API")
                        .description("API for managing wellness resources in the Student Wellness Hub. " +
                                "This service provides endpoints to create, read, update, and delete wellness resources, " +
                                "search resources by category or keyword, and track resource popularity statistics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GBC Wellness Hub Team")
                                .email("support@gbc-wellness.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/resources").description("Gateway Server (Local)"),
                        new Server().url("http://localhost:8081").description("Direct Service (Local)"),
                        new Server().url("http://wellness-resource-service:8081").description("Container Server")
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

