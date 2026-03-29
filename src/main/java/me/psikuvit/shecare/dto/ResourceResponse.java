package me.psikuvit.shecare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {
    
    private String id;
    
    private String title;
    
    private String description;
    
    private String category;
    
    private Boolean featured;
    
    private String imageUrl;
    
    private String duration;
    
    private String author;
}

