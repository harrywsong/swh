package ca.gbc.comp3095.wellnessresourceservice.dto;

public record WellnessResourceRequest(
        String title,
        String description,
        String category,
        String url
) {
}