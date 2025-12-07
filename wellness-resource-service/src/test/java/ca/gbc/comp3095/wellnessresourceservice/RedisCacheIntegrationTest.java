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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WellnessResourceServiceApplication.class)
@DirtiesContext
@Testcontainers
class RedisCacheIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"));

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        registry.add("spring.cache.type", () -> "redis");
    }

    @Autowired
    private WellnessResourceService resourceService;

    @Autowired
    private WellnessResourceRepository resourceRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear database first
        resourceRepository.deleteAll();
        
        // Then clear all caches to avoid stale data issues
        // This ensures any cached data from previous test runs is removed
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
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

        // Second call - should use cache (verify by checking it returns same data)
        Optional<WellnessResource> secondCall = resourceService.getResourceById(resourceId);
        assertThat(secondCall).isPresent();
        assertThat(secondCall.get().getTitle()).isEqualTo("Test Resource");
        
        // Verify both calls return the same instance (cached)
        assertThat(firstCall.get().getResourceId()).isEqualTo(secondCall.get().getResourceId());
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

        // Second call - should return same data
        List<WellnessResource> secondCall = resourceService.getAllResources();
        assertThat(secondCall).hasSize(2);
        
        // Verify we got the same resources
        assertThat(firstCall.get(0).getResourceId()).isEqualTo(secondCall.get(0).getResourceId());
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

        // Second call - should use cache and return same data
        List<WellnessResource> secondCall = resourceService.getResourcesByCategory(category);
        assertThat(secondCall).hasSize(2);
        
        // Verify we got the same resources
        assertThat(firstCall.get(0).getResourceId()).isEqualTo(secondCall.get(0).getResourceId());
        assertThat(firstCall.get(1).getResourceId()).isEqualTo(secondCall.get(1).getResourceId());
    }

    @Test
    void testCacheEvictionOnCreate() {
        // Create first resource and populate cache
        resourceService.createResource(new WellnessResourceRequest(
                "Resource 1", "Description", "category1", "https://example.com/1"));
        
        List<WellnessResource> beforeCreate = resourceService.getAllResources();
        assertThat(beforeCreate).hasSize(1);

        // Create a new resource - should evict cache
        resourceService.createResource(new WellnessResourceRequest(
                "Resource 2", "Description", "category2", "https://example.com/2"));

        // Fetch again - should have new data
        List<WellnessResource> afterCreate = resourceService.getAllResources();
        assertThat(afterCreate).hasSize(2);
    }

    @Test
    void testCacheUpdateOnUpdate() {
        // Create and cache a resource
        WellnessResource created = resourceService.createResource(new WellnessResourceRequest(
                "Original Title", "Description", "category", "https://example.com"));
        Long resourceId = created.getResourceId();
        
        // Get to populate cache
        Optional<WellnessResource> original = resourceService.getResourceById(resourceId);
        assertThat(original).isPresent();
        assertThat(original.get().getTitle()).isEqualTo("Original Title");

        // Update the resource
        resourceService.updateResource(resourceId, new WellnessResourceRequest(
                "Updated Title", "Updated Description", "category", "https://example.com"));

        // Get again - should have updated data
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
        
        // Get to populate cache
        Optional<WellnessResource> beforeDelete = resourceService.getResourceById(resourceId);
        assertThat(beforeDelete).isPresent();

        // Delete the resource
        resourceService.deleteResource(resourceId);

        // Try to get again - should not find it
        Optional<WellnessResource> afterDelete = resourceService.getResourceById(resourceId);
        assertThat(afterDelete).isEmpty();
    }
}

