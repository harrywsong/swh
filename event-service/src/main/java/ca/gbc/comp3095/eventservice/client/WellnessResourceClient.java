package ca.gbc.comp3095.eventservice.client;

import ca.gbc.comp3095.eventservice.dto.WellnessResource;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WellnessResourceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${wellness.resource.service.url}")
    private String wellnessResourceServiceUrl;

    @CircuitBreaker(name = "wellnessResourceService", fallbackMethod = "getAllResourcesFallback")
    public List<WellnessResource> getAllResources() {
        log.info("Calling wellness-resource-service to get all resources");
        
        try {
            WebClient webClient = webClientBuilder.baseUrl(wellnessResourceServiceUrl).build();
            
            return webClient.get()
                    .uri("/api/resources")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<WellnessResource>>() {})
                    .doOnSuccess(resources -> log.info("Successfully retrieved {} resources", 
                            resources != null ? resources.size() : 0))
                    .doOnError(error -> log.error("Error calling wellness-resource-service", error))
                    .block();
        } catch (Exception e) {
            log.error("Exception caught in getAllResources, re-throwing for circuit breaker: {}", e.getClass().getSimpleName());
            throw e;
        }
    }

    @CircuitBreaker(name = "wellnessResourceService", fallbackMethod = "getResourceByIdFallback")
    public WellnessResource getResourceById(Long id) {
        log.info("Calling wellness-resource-service to get resource with id: {}", id);
        
        try {
            WebClient webClient = webClientBuilder.baseUrl(wellnessResourceServiceUrl).build();
            
            return webClient.get()
                    .uri("/api/resources/{id}", id)
                    .retrieve()
                    .bodyToMono(WellnessResource.class)
                    .doOnSuccess(resource -> log.info("Successfully retrieved resource with id: {}", id))
                    .doOnError(error -> log.error("Error calling wellness-resource-service for resource id: {}", id, error))
                    .block();
        } catch (Exception e) {
            log.error("Exception caught in getResourceById, re-throwing for circuit breaker: {}", e.getClass().getSimpleName());
            throw e;
        }
    }

    // Fallback methods
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

    public WellnessResource getResourceByIdFallback(Long id, Throwable ex) {
        log.warn("Circuit breaker fallback triggered for getResourceById with id: {}. Exception: {} - {}", 
                id, ex.getClass().getSimpleName(), ex.getMessage());
        
        if (ex instanceof CallNotPermittedException) {
            log.error("Circuit breaker is OPEN - service is unavailable. Returning default response.");
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
        
        // Return default response
        return new WellnessResource(id, "Resource unavailable", 
                "The wellness resource service is currently unavailable. Please try again later.", 
                "Unknown", "");
    }
}

