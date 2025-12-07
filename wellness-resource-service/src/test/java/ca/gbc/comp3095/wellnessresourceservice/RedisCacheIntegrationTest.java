package ca.gbc.comp3095.wellnessresourceservice;

import ca.gbc.comp3095.wellnessresourceservice.dto.WellnessResourceRequest;
import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;
import ca.gbc.comp3095.wellnessresourceservice.repository.WellnessResourceRepository;
import ca.gbc.comp3095.wellnessresourceservice.service.WellnessResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
class RedisCacheIntegrationTest {

    @Autowired
    private WellnessResourceService resourceService;

    @Autowired
    private WellnessResourceRepository resourceRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        resourceRepository.deleteAll();
        // Clear all caches
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).clear();
        });
    }

    @Test
    void testGetResourceByIdUsesCache() {
        // Create a resource
        WellnessResourceRequest request = new WellnessResourceRequest(
                "Test Resource",
                "Test Description",
                "test-category",
                "https://example.com"
        );
        WellnessResource created = resourceService.createResource(request);
        Long resourceId = created.getResourceId();

        // First call - should hit database and populate cache
        Optional<WellnessResource> firstCall = resourceService.getResourceById(resourceId);
        assertThat(firstCall).isPresent();
        assertThat(firstCall.get().getTitle()).isEqualTo("Test Resource");

        // Verify cache was populated
        assertThat(cacheManager.getCache("resources")).isNotNull();
        assertThat(cacheManager.getCache("resources").get(resourceId)).isNotNull();

        // Delete from database to verify cache is being used
        resourceRepository.deleteById(resourceId);

        // Second call - should return from cache (even though DB is empty)
        Optional<WellnessResource> secondCall = resourceService.getResourceById(resourceId);
        assertThat(secondCall).isPresent();
        assertThat(secondCall.get().getTitle()).isEqualTo("Test Resource");
    }

    @Test
    void testGetAllResourcesUsesCache() {
        // Create multiple resources
        resourceService.createResource(new WellnessResourceRequest(
                "Resource 1", "Description 1", "category1", "https://example.com/1"));
        resourceService.createResource(new WellnessResourceRequest(
                "Resource 2", "Description 2", "category2", "https://example.com/2"));

        // First call - should hit database
        List<WellnessResource> firstCall = resourceService.getAllResources();
        assertThat(firstCall).hasSize(2);

        // Verify cache was populated
        assertThat(cacheManager.getCache("resources")).isNotNull();
        assertThat(cacheManager.getCache("resources").get("all")).isNotNull();

        // Delete all from database
        resourceRepository.deleteAll();

        // Second call - should return from cache
        List<WellnessResource> secondCall = resourceService.getAllResources();
        assertThat(secondCall).hasSize(2);
    }

    @Test
    void testGetResourcesByCategoryUsesCache() {
        String category = "wellness";
        
        // Create resources in the category
        resourceService.createResource(new WellnessResourceRequest(
                "Wellness Resource 1", "Description", category, "https://example.com/1"));
        resourceService.createResource(new WellnessResourceRequest(
                "Wellness Resource 2", "Description", category, "https://example.com/2"));

        // First call - should hit database
        List<WellnessResource> firstCall = resourceService.getResourcesByCategory(category);
        assertThat(firstCall).hasSize(2);

        // Verify cache was populated
        assertThat(cacheManager.getCache("resourcesByCategory")).isNotNull();
        assertThat(cacheManager.getCache("resourcesByCategory").get(category)).isNotNull();

        // Delete all from database
        resourceRepository.deleteAll();

        // Second call - should return from cache
        List<WellnessResource> secondCall = resourceService.getResourcesByCategory(category);
        assertThat(secondCall).hasSize(2);
    }

    @Test
    void testCacheEvictionOnCreate() {
        // Populate cache
        resourceService.createResource(new WellnessResourceRequest(
                "Resource 1", "Description", "category1", "https://example.com/1"));
        resourceService.getAllResources(); // Populate cache

        // Verify cache is populated
        assertThat(cacheManager.getCache("resources").get("all")).isNotNull();

        // Create a new resource - should evict cache
        resourceService.createResource(new WellnessResourceRequest(
                "Resource 2", "Description", "category2", "https://example.com/2"));

        // Cache should be evicted
        assertThat(cacheManager.getCache("resources").get("all")).isNull();
    }

    @Test
    void testCacheEvictionOnUpdate() {
        // Create and cache a resource
        WellnessResource created = resourceService.createResource(new WellnessResourceRequest(
                "Original Title", "Description", "category", "https://example.com"));
        Long resourceId = created.getResourceId();
        
        resourceService.getResourceById(resourceId); // Populate cache

        // Verify cache is populated
        assertThat(cacheManager.getCache("resources").get(resourceId)).isNotNull();

        // Update the resource - should evict and update cache
        resourceService.updateResource(resourceId, new WellnessResourceRequest(
                "Updated Title", "Updated Description", "category", "https://example.com"));

        // Cache should be updated (CachePut)
        assertThat(cacheManager.getCache("resources").get(resourceId)).isNotNull();
        Optional<WellnessResource> updated = resourceService.getResourceById(resourceId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void testCacheEvictionOnDelete() {
        // Create and cache a resource
        WellnessResource created = resourceService.createResource(new WellnessResourceRequest(
                "To Delete", "Description", "category", "https://example.com"));
        Long resourceId = created.getResourceId();
        
        resourceService.getResourceById(resourceId); // Populate cache

        // Verify cache is populated
        assertThat(cacheManager.getCache("resources").get(resourceId)).isNotNull();

        // Delete the resource - should evict cache
        resourceService.deleteResource(resourceId);

        // Cache should be evicted
        assertThat(cacheManager.getCache("resources").get(resourceId)).isNull();
    }
}

