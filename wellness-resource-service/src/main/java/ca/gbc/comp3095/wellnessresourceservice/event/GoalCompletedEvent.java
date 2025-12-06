package ca.gbc.comp3095.wellnessresourceservice.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalCompletedEvent {
    private String goalId;
    private String title;
    private String category;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;
    
    private String eventType = "GOAL_COMPLETED";
}
