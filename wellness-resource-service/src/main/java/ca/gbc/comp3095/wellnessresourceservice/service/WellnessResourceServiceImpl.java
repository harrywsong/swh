package ca.gbc.comp3095.wellnessresourceservice.service;

import ca.gbc.comp3095.wellnessresourceservice.dto.WellnessResourceRequest;
import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;
import ca.gbc.comp3095.wellnessresourceservice.repository.WellnessResourceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class WellnessResourceServiceImpl implements WellnessResourceService {

    private final WellnessResourceRepository repository;

    @Override
    @Cacheable(value = "resources", key = "'all'")
    public List<WellnessResource> getAllResources() {
        log.info("Fetching all resources from database");
        return repository.findAll();
    }

    @Override
    @Cacheable(value = "resources", key = "#id")
    public Optional<WellnessResource> getResourceById(Long id) {
        log.info("Fetching resource with id: {} from database", id);
        return repository.findById(id);
    }

    @Override
    @Cacheable(value = "resourcesByCategory", key = "#category")
    public List<WellnessResource> getResourcesByCategory(String category) {
        log.info("Fetching resources for category: {} from database", category);
        return repository.findAll().stream()
                .filter(resource -> resource.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    @Override
    public List<WellnessResource> searchByKeyword(String keyword) {
        log.info("Searching resources with keyword: {}", keyword);
        return repository.findAll().stream()
                .filter(resource -> resource.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"resources", "resourcesByCategory"}, allEntries = true)
    public WellnessResource createResource(WellnessResourceRequest request) {
        log.info("Creating new resource: {}", request.title());

        WellnessResource resource = WellnessResource.builder()
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .url(request.url())
                .build();

        return repository.save(resource);
    }

    @Override
    @CachePut(value = "resources", key = "#id")
    @CacheEvict(value = "resourcesByCategory", allEntries = true)
    public WellnessResource updateResource(Long id, WellnessResourceRequest request) {
        log.info("Updating resource with id: {}", id);
        WellnessResource resource = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found with id: " + id));

        resource.setTitle(request.title());
        resource.setDescription(request.description());
        resource.setCategory(request.category());
        resource.setUrl(request.url());

        return repository.save(resource);
    }

    @Override
    @CacheEvict(value = {"resources", "resourcesByCategory"}, allEntries = true)
    public void deleteResource(Long id) {
        log.info("Deleting resource with id: {}", id);
        repository.deleteById(id);
    }
}