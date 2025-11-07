package ca.gbc.comp3095.wellnessresourceservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="t_wellness_resource")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WellnessResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    private String title;

    private String description;

    private String category;

    private String url;
}
