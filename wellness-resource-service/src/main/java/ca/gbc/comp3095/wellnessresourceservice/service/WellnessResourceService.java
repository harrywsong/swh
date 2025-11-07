package ca.gbc.comp3095.wellnessresourceservice.service;

import ca.gbc.comp3095.wellnessresourceservice.dto.WellnessResourceRequest;
import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;

import java.util.List;
import java.util.Optional;

public interface WellnessResourceService {

    List<WellnessResource> getAllResources();

    Optional<WellnessResource> getResourceById(Long id);

    List<WellnessResource> getResourcesByCategory(String category);

    List<WellnessResource> searchByKeyword(String keyword);

    WellnessResource createResource(WellnessResourceRequest request);  // Changed

    WellnessResource updateResource(Long id, WellnessResourceRequest request);  // Changed

    void deleteResource(Long id);
}