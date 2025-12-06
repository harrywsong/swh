package ca.gbc.comp3095.wellnessresourceservice.controller;

import ca.gbc.comp3095.wellnessresourceservice.dto.WellnessResourceRequest;
import ca.gbc.comp3095.wellnessresourceservice.model.ResourcePopularityTracker;
import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;
import ca.gbc.comp3095.wellnessresourceservice.repository.ResourcePopularityRepository;
import ca.gbc.comp3095.wellnessresourceservice.service.WellnessResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(name = "Wellness Resources", description = "APIs for managing wellness resources")
public class WellnessResourceController {

    private final WellnessResourceService service;
    private final ResourcePopularityRepository popularityRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all wellness resources",
            description = "Retrieves a list of all available wellness resources in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of resources",
                    content = @Content(schema = @Schema(implementation = WellnessResource.class)))
    })
    public List<WellnessResource> getAllResources() {
        return service.getAllResources();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get resource by ID",
            description = "Retrieves a specific wellness resource by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource found",
                    content = @Content(schema = @Schema(implementation = WellnessResource.class))),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public WellnessResource getResourceById(
            @Parameter(description = "Resource ID", required = true) @PathVariable Long id) {
        return service.getResourceById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found with id: " + id));
    }

    @GetMapping("/category/{category}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get resources by category",
            description = "Retrieves all wellness resources filtered by a specific category."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved resources",
                    content = @Content(schema = @Schema(implementation = WellnessResource.class)))
    })
    public List<WellnessResource> getResourcesByCategory(
            @Parameter(description = "Category name", required = true) @PathVariable String category) {
        return service.getResourcesByCategory(category);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Search resources by keyword",
            description = "Searches wellness resources by a keyword matching title or description."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = WellnessResource.class)))
    })
    public List<WellnessResource> searchResources(
            @Parameter(description = "Search keyword", required = true) @RequestParam String keyword) {
        return service.searchByKeyword(keyword);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new wellness resource",
            description = "Creates a new wellness resource. Requires staff role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Resource created successfully",
                    content = @Content(schema = @Schema(implementation = WellnessResource.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Staff role required")
    })
    public WellnessResource createResource(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Wellness resource details", required = true,
                    content = @Content(schema = @Schema(implementation = WellnessResourceRequest.class)))
            @RequestBody WellnessResourceRequest request) {
        return service.createResource(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update a wellness resource",
            description = "Updates an existing wellness resource. Requires staff role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource updated successfully",
                    content = @Content(schema = @Schema(implementation = WellnessResource.class))),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Staff role required")
    })
    public WellnessResource updateResource(
            @Parameter(description = "Resource ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated wellness resource details", required = true,
                    content = @Content(schema = @Schema(implementation = WellnessResourceRequest.class)))
            @RequestBody WellnessResourceRequest request) {
        return service.updateResource(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a wellness resource",
            description = "Deletes a wellness resource by ID. Requires staff role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Staff role required")
    })
    public void deleteResource(
            @Parameter(description = "Resource ID", required = true) @PathVariable Long id) {
        service.deleteResource(id);
    }

    @GetMapping("/popularity/category/{category}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get popularity statistics by category",
            description = "Retrieves popularity tracking statistics for resources in a specific category."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Popularity statistics retrieved",
                    content = @Content(schema = @Schema(implementation = ResourcePopularityTracker.class)))
    })
    public ResourcePopularityTracker getCategoryPopularity(
            @Parameter(description = "Category name", required = true) @PathVariable String category) {
        return popularityRepository.findByCategory(category)
                .orElse(ResourcePopularityTracker.builder()
                        .category(category)
                        .viewCount(0)
                        .goalCompletionCount(0)
                        .build());
    }

    @GetMapping("/popularity/all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all popularity statistics",
            description = "Retrieves popularity tracking statistics for all resource categories."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All popularity statistics retrieved",
                    content = @Content(schema = @Schema(implementation = ResourcePopularityTracker.class)))
    })
    public List<ResourcePopularityTracker> getAllPopularityStats() {
        return popularityRepository.findAll();
    }
}
