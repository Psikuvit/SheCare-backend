package me.psikuvit.shecare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private String id;
    private String userId;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;
}

