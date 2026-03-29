package me.psikuvit.shecare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.ResourceResponse;
import me.psikuvit.shecare.exception.ResourceNotFoundException;
import me.psikuvit.shecare.model.Resource;
import me.psikuvit.shecare.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {
    
    private final ResourceRepository resourceRepository;
    
    public List<ResourceResponse> getAllResources() {
        log.info("Getting all resources");
        return resourceRepository.findAll().stream()
                .map(this::toResourceResponse)
                .collect(Collectors.toList());
    }
    
    public List<ResourceResponse> getResourcesByCategory(String category) {
        log.info("Getting resources by category: {}", category);
        return resourceRepository.findByCategory(category).stream()
                .map(this::toResourceResponse)
                .collect(Collectors.toList());
    }
    
    public List<ResourceResponse> getFeaturedResources() {
        log.info("Getting featured resources");
        return resourceRepository.findByFeaturedTrue().stream()
                .map(this::toResourceResponse)
                .collect(Collectors.toList());
    }
    
    public ResourceResponse getResource(String resourceId) {
        log.info("Getting resource: {}", resourceId);
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        return toResourceResponse(resource);
    }
    
    private ResourceResponse toResourceResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .category(resource.getCategory())
                .featured(resource.getFeatured())
                .imageUrl(resource.getImageUrl())
                .duration(resource.getDuration())
                .author(resource.getAuthor())
                .build();
    }
}

