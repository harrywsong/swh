package ca.gbc.comp3095.wellnessresourceservice.controller;

import ca.gbc.comp3095.wellnessresourceservice.dto.WellnessResourceRequest;
import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;
import ca.gbc.comp3095.wellnessresourceservice.service.WellnessResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class WellnessResourceController {

    private final WellnessResourceService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<WellnessResource> getAllResources() {
        return service.getAllResources();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WellnessResource getResourceById(@PathVariable Long id) {
        return service.getResourceById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found with id: " + id));
    }

    @GetMapping("/category/{category}")
    @ResponseStatus(HttpStatus.OK)
    public List<WellnessResource> getResourcesByCategory(@PathVariable String category) {
        return service.getResourcesByCategory(category);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<WellnessResource> searchResources(@RequestParam String keyword) {
        return service.searchByKeyword(keyword);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WellnessResource createResource(@RequestBody WellnessResourceRequest request) {  // Changed
        return service.createResource(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WellnessResource updateResource(@PathVariable Long id, @RequestBody WellnessResourceRequest request) {  // Changed
        return service.updateResource(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@PathVariable Long id) {
        service.deleteResource(id);
    }
}
