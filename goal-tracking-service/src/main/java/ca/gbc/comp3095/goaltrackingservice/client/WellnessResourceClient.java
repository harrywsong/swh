package ca.gbc.comp3095.goaltrackingservice.client;

import ca.gbc.comp3095.goaltrackingservice.dto.WellnessResource;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WellnessResourceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${wellness.resource.service.url}")
    private String wellnessResourceServiceUrl;

    @CircuitBreaker(name = "wellnessResourceService", fallbackMethod = "getResourcesByCategoryFallback")
    public List<WellnessResource> getResourcesByCategory(String category) {
        log.info("Calling wellness-resource-service to get resources for category: {}", category);
        
        WebClient webClient = webClientBuilder.baseUrl(wellnessResourceServiceUrl).build();
        
        return webClient.get()
                .uri("/api/resources/category/{category}", category)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<WellnessResource>>() {})
                .doOnSuccess(resources -> log.info("Successfully retrieved {} resources for category: {}", 
                        resources != null ? resources.size() : 0, category))
                .doOnError(error -> log.error("Error calling wellness-resource-service for category: {}", category, error))
                .block();
    }

    @CircuitBreaker(name = "wellnessResourceService", fallbackMethod = "getAllResourcesFallback")
    public List<WellnessResource> getAllResources() {
        log.info("Calling wellness-resource-service to get all resources");
        
        WebClient webClient = webClientBuilder.baseUrl(wellnessResourceServiceUrl).build();
        
        return webClient.get()
                .uri("/api/resources")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<WellnessResource>>() {})
                .doOnSuccess(resources -> log.info("Successfully retrieved {} resources", 
                        resources != null ? resources.size() : 0))
                .doOnError(error -> log.error("Error calling wellness-resource-service", error))
                .block();
    }

    // Fallback methods
    public List<WellnessResource> getResourcesByCategoryFallback(String category, Throwable ex) {
        log.warn("Circuit breaker fallback triggered for category: {}. Exception: {} - {}", 
                category, ex.getClass().getSimpleName(), ex.getMessage());
        
        if (ex instanceof CallNotPermittedException) {
            log.error("Circuit breaker is OPEN - service is unavailable. Returning cached/default response.");
        } else if (ex instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) ex;
            log.error("HTTP error from wellness-resource-service: {} - {}", 
                    webEx.getStatusCode(), webEx.getMessage());
        } else if (ex instanceof WebClientRequestException) {
            log.error("Connection error - wellness-resource-service is unreachable: {}", ex.getMessage());
        } else if (ex.getCause() != null && ex.getCause() instanceof java.net.ConnectException) {
            log.error("Connection refused - wellness-resource-service is down: {}", ex.getMessage());
        } else if (ex.getCause() != null && ex.getCause() instanceof io.netty.channel.ConnectTimeoutException) {
            log.error("Connection timeout - wellness-resource-service is unreachable: {}", ex.getMessage());
        } else {
            log.error("Unexpected error calling wellness-resource-service: {} - {}", 
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
        
        // Return default/cached response
        return Collections.emptyList();
    }

    public List<WellnessResource> getAllResourcesFallback(Throwable ex) {
        log.warn("Circuit breaker fallback triggered for getAllResources. Exception: {} - {}", 
                ex.getClass().getSimpleName(), ex.getMessage());
        
        if (ex instanceof CallNotPermittedException) {
            log.error("Circuit breaker is OPEN - service is unavailable. Returning cached/default response.");
        } else if (ex instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) ex;
            log.error("HTTP error from wellness-resource-service: {} - {}", 
                    webEx.getStatusCode(), webEx.getMessage());
        } else if (ex instanceof WebClientRequestException) {
            log.error("Connection error - wellness-resource-service is unreachable: {}", ex.getMessage());
        } else if (ex.getCause() != null && ex.getCause() instanceof java.net.ConnectException) {
            log.error("Connection refused - wellness-resource-service is down: {}", ex.getMessage());
        } else if (ex.getCause() != null && ex.getCause() instanceof io.netty.channel.ConnectTimeoutException) {
            log.error("Connection timeout - wellness-resource-service is unreachable: {}", ex.getMessage());
        } else {
            log.error("Unexpected error calling wellness-resource-service: {} - {}", 
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
        
        // Return default/cached response
        return Collections.emptyList();
    }
}

