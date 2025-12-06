package ca.gbc.comp3095.wellnessresourceservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_resource_popularity")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourcePopularityTracker {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String category;
    
    private Integer viewCount = 0;
    
    private Integer goalCompletionCount = 0;
    
    @Column(name = "last_updated")
    private java.time.LocalDateTime lastUpdated;
}
