package me.psikuvit.shecare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    @JsonProperty("post_id")
    private String postId;
    
    @JsonProperty("author_id")
    private String authorId;
    
    private String title;
    
    private String authorName;
    
    private String content;
    
    private List<String> tags;
    
    private String createdAt;
    
    private int likes;
    
    private int comments;
    
    private int shares;
}

