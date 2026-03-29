package me.psikuvit.shecare.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.ResourceResponse;
import me.psikuvit.shecare.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {
    
    private final ResourceService resourceService;
    
    /**
     * Get all resources (public endpoint)
     * GET /api/v1/resources
     */
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAllResources() {
        log.info("Get all resources endpoint called");
        return ResponseEntity.ok(resourceService.getAllResources());
    }
    
    /**
     * Get resources by category (public endpoint)
     * GET /api/v1/resources/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ResourceResponse>> getResourcesByCategory(@PathVariable String category) {
        log.info("Get resources by category: {}", category);
        return ResponseEntity.ok(resourceService.getResourcesByCategory(category));
    }
    
    /**
     * Get featured resources (public endpoint)
     * GET /api/v1/resources/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<List<ResourceResponse>> getFeaturedResources() {
        log.info("Get featured resources endpoint called");
        return ResponseEntity.ok(resourceService.getFeaturedResources());
    }
    
    /**
     * Get single resource by ID (public endpoint)
     * GET /api/v1/resources/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResource(@PathVariable String id) {
        log.info("Get resource: {}", id);
        return ResponseEntity.ok(resourceService.getResource(id));
    }
}

