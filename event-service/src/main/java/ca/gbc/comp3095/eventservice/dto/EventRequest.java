package ca.gbc.comp3095.eventservice.dto;

import java.time.LocalDateTime;

public record EventRequest(
        String title,
        String description,
        LocalDateTime date,
        String location,
        Integer capacity
) {
}