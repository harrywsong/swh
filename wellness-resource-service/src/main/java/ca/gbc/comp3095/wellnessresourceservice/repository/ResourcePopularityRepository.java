package ca.gbc.comp3095.wellnessresourceservice.repository;

import ca.gbc.comp3095.wellnessresourceservice.model.ResourcePopularityTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourcePopularityRepository extends JpaRepository<ResourcePopularityTracker, Long> {
    Optional<ResourcePopularityTracker> findByCategory(String category);
}
