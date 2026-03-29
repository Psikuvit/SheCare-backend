package me.psikuvit.shecare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.PostRequest;
import me.psikuvit.shecare.dto.PostResponse;
import me.psikuvit.shecare.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    
    private final PostService postService;
    
    /**
     * Get all posts
     * GET /api/v1/posts
     */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        log.info("Get all posts endpoint called");
        return ResponseEntity.ok(postService.getAllPosts());
    }
    
    /**
     * Create a new post
     * POST /api/v1/posts
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        log.info("Create post endpoint called");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        PostResponse response = postService.createPost(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get a post by ID
     * GET /api/v1/posts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable String id) {
        log.info("Get post endpoint called for id: {}", id);
        return ResponseEntity.ok(postService.getPost(id));
    }
    
    /**
     * Update a post
     * PUT /api/v1/posts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String id,
            @Valid @RequestBody PostRequest request) {
        log.info("Update post endpoint called for id: {}", id);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        PostResponse response = postService.updatePost(userId, id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a post
     * DELETE /api/v1/posts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        log.info("Delete post endpoint called for id: {}", id);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        postService.deletePost(userId, id);
        return ResponseEntity.noContent().build();
    }
}

