package me.psikuvit.shecare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.PostRequest;
import me.psikuvit.shecare.dto.PostResponse;
import me.psikuvit.shecare.exception.ResourceNotFoundException;
import me.psikuvit.shecare.model.Post;
import me.psikuvit.shecare.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    
    private final PostRepository postRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public PostResponse createPost(String userId, PostRequest request) {
        log.info("Creating post for user: {}", userId);
        
        Post post = Post.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        
        post = postRepository.save(post);
        return toPostResponse(post);
    }
    
    public List<PostResponse> getUserPosts(String userId) {
        log.info("Getting posts for user: {}", userId);
        return postRepository.findByUserId(userId).stream()
                .map(this::toPostResponse)
                .collect(Collectors.toList());
    }
    
    public List<PostResponse> getAllPosts() {
        log.info("Getting all posts");
        return postRepository.findAll().stream()
                .map(this::toPostResponse)
                .collect(Collectors.toList());
    }
    
    public PostResponse getPost(String postId) {
        log.info("Getting post: {}", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return toPostResponse(post);
    }
    
    public PostResponse updatePost(String userId, String postId, PostRequest request) {
        log.info("Updating post: {} for user: {}", postId, userId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Cannot update post of another user");
        }
        
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        
        post = postRepository.save(post);
        return toPostResponse(post);
    }
    
    public void deletePost(String userId, String postId) {
        log.info("Deleting post: {} for user: {}", postId, userId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Cannot delete post of another user");
        }
        
        postRepository.delete(post);
    }
    
    private PostResponse toPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt().format(FORMATTER))
                .updatedAt(post.getUpdatedAt().format(FORMATTER))
                .build();
    }
}

