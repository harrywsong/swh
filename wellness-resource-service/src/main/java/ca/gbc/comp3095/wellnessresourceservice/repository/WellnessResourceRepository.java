package ca.gbc.comp3095.wellnessresourceservice.repository;

import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WellnessResourceRepository extends JpaRepository<WellnessResource, Long> {
}