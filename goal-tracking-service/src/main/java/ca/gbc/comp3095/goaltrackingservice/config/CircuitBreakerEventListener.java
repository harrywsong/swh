package ca.gbc.comp3095.goaltrackingservice.config;

import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CircuitBreakerEventListener {

    @EventListener
    public void onCircuitBreakerEvent(CircuitBreakerEvent event) {
        log.info("Circuit Breaker Event: {} - {}", event.getCircuitBreakerName(), event.getEventType());
        
        if (event instanceof CircuitBreakerOnStateTransitionEvent) {
            CircuitBreakerOnStateTransitionEvent stateEvent = (CircuitBreakerOnStateTransitionEvent) event;
            log.warn("Circuit Breaker State Transition: {} - From {} to {}", 
                    stateEvent.getCircuitBreakerName(),
                    stateEvent.getStateTransition().getFromState(),
                    stateEvent.getStateTransition().getToState());
        }
    }
}

