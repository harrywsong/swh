package ca.gbc.comp3095.eventservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalTracking {
    @JsonProperty("goalId")
    private String goalId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("targetDate")
    private LocalDate targetDate;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("category")
    private String category;
}

