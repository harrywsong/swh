package ca.gbc.comp3095.goaltrackingservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "goal_tracking")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoalTracking {

    @Id
    private String goalId;

    private String title;

    private String description;

    private LocalDate targetDate;

    private String status;

    private String category;
}