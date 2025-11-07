package ca.gbc.comp3095.goaltrackingservice.dto;

import java.time.LocalDate;

public record GoalTrackingRequest(
        String title,
        String description,
        LocalDate targetDate,
        String status,
        String category
) {
}