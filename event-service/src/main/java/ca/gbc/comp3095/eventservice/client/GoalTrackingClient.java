package ca.gbc.comp3095.eventservice.client;

import ca.gbc.comp3095.eventservice.dto.GoalTracking;
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
public class GoalTrackingClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${goal.tracking.service.url:http://goal-tracking-service:8082}")
    private String goalTrackingServiceUrl;

    @CircuitBreaker(name = "goalTrackingService", fallbackMethod = "getGoalsByCategoryFallback")
    public List<GoalTracking> getGoalsByCategory(String category) {
        log.info("Calling goal-tracking-service to get goals for category: {}", category);
        
        try {
            WebClient webClient = webClientBuilder.baseUrl(goalTrackingServiceUrl).build();
            
            return webClient.get()
                    .uri("/api/goals/category/{category}", category)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GoalTracking>>() {})
                    .doOnSuccess(goals -> log.info("Successfully retrieved {} goals for category: {}", 
                            goals != null ? goals.size() : 0, category))
                    .doOnError(error -> log.error("Error calling goal-tracking-service for category: {}", category, error))
                    .block();
        } catch (Exception e) {
            log.error("Exception caught in getGoalsByCategory, re-throwing for circuit breaker: {}", e.getClass().getSimpleName());
            throw e;
        }
    }

    // Fallback method
    public List<GoalTracking> getGoalsByCategoryFallback(String category, Throwable ex) {
        log.warn("Circuit breaker fallback triggered for category: {}. Exception: {} - {}", 
                category, ex.getClass().getSimpleName(), ex.getMessage());
        
        if (ex instanceof CallNotPermittedException) {
            log.error("Circuit breaker is OPEN - goal-tracking-service is unavailable. Returning cached/default response.");
        } else if (ex instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) ex;
            log.error("HTTP error from goal-tracking-service: {} - {}", 
                    webEx.getStatusCode(), webEx.getMessage());
        } else if (ex instanceof WebClientRequestException) {
            log.error("Connection error - goal-tracking-service is unreachable: {}", ex.getMessage());
        } else if (ex.getCause() != null && ex.getCause() instanceof java.net.ConnectException) {
            log.error("Connection refused - goal-tracking-service is down: {}", ex.getMessage());
        } else if (ex.getCause() != null && ex.getCause() instanceof io.netty.channel.ConnectTimeoutException) {
            log.error("Connection timeout - goal-tracking-service is unreachable: {}", ex.getMessage());
        } else {
            log.error("Unexpected error calling goal-tracking-service: {} - {}", 
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
        
        // Return default/cached response
        return Collections.emptyList();
    }
}

