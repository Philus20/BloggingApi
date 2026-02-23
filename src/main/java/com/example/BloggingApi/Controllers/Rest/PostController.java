package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Requests.CreatePostRequest;
import com.example.BloggingApi.DTOs.Requests.EditPostRequest;
import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.PostsResponse;
import com.example.BloggingApi.Services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Posts", description = "Blog post CRUD and search")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts/{id}")
    @Operation(summary = "Get post by ID")
    public ApiResponse<PostsResponse> getPostById(@Parameter(description = "Post ID") @PathVariable Long id) {
        return ApiResponse.success("Post retrieved successfully", PostsResponse.from(postService.getById(id)));
    }

    @GetMapping("/posts")
    @Operation(summary = "Get all posts", description = "Paginated list with optional sorting")
    public ApiResponse<Page<PostsResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success("Posts retrieved successfully", postService.getAll(page, size, sortBy, ascending).map(PostsResponse::from));
    }

    @GetMapping("/posts/search")
    @Operation(summary = "Search posts", description = "Search by keyword, title, or author")
    public ApiResponse<Page<PostsResponse>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) {
        return ApiResponse.success("Posts search completed successfully", postService.search(keyword, title, author, page, size, sortBy, ascending).map(PostsResponse::from));
    }

    @PostMapping("/posts")
    @Operation(summary = "Create a new post")
    public ApiResponse<PostsResponse> createPost(@RequestBody @jakarta.validation.Valid CreatePostRequest request) {
        return ApiResponse.success("Success", PostsResponse.from(postService.create(request)));
    }

    @PutMapping("/posts")
    @Operation(summary = "Update a post")
    public ApiResponse<PostsResponse> editPost(@RequestBody @jakarta.validation.Valid EditPostRequest request) {
        return ApiResponse.success("Success", PostsResponse.from(postService.update(request)));
    }

    @DeleteMapping("/posts/{id}")
    @Operation(summary = "Delete a post")
    public ApiResponse<Void> deletePost(@Parameter(description = "Post ID") @PathVariable Long id) {
        postService.delete(id);
        return ApiResponse.success("Post deleted");
    }

}
