package com.example.BloggingApi.Controllers.RestControllers;

import com.example.BloggingApi.RequestsDTO.CreatePostRequest;
import com.example.BloggingApi.RequestsDTO.EditPostRequest;
import com.example.BloggingApi.ResposesDTO.ApiResponse;
import com.example.BloggingApi.ResposesDTO.PostsResponse;
import com.example.BloggingApi.Services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Posts", description = "Create, read, update, and delete blog posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Get post by ID", description = "Returns a single post by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{id}")
    public ApiResponse<PostsResponse> getPostById(
            @Parameter(description = "Post ID") @PathVariable Long id) {
        return ApiResponse.success(
                "Post retrieved successfully",
                postService.getPostById(id)
        );
    }

    @Operation(summary = "List all posts", description = "Returns a paginated and sorted list of all posts")
    @GetMapping
    public ApiResponse<Page<PostsResponse>> getAllPosts(
            @Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "5") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort ascending") @RequestParam(defaultValue = "false") boolean ascending
    ) {
        return ApiResponse.success(
                "Posts retrieved successfully",
                postService.getAllPosts(page, size, sortBy, ascending)
        );
    }

    @Operation(summary = "Search posts", description = "Search posts by keyword, title, or author. At least one search parameter is required.")
    @GetMapping("/search")
    public ApiResponse<Page<PostsResponse>> searchPosts(
            @Parameter(description = "Search in title and content") @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by title containing") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by author username") @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) {
        return ApiResponse.success(
                "Posts search completed successfully",
                postService.searchPosts(keyword, title, author, page, size, sortBy, ascending)
        );
    }

    @Operation(summary = "Create post", description = "Creates a new post. Requires title, content, and authorId.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or author not found")
    })
    @PostMapping
    public ApiResponse<PostsResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ApiResponse.success(
                "Post created successfully",
                postService.createPost(request)
        );
    }

    @Operation(summary = "Update post", description = "Updates an existing post by ID.")
    @PutMapping
    public ApiResponse<PostsResponse> editPost(@Valid @RequestBody EditPostRequest request) {
        return ApiResponse.success(
                "Post updated successfully",
                postService.editPost(request)
        );
    }

    @Operation(summary = "Delete post", description = "Deletes a post by ID.")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(@Parameter(description = "Post ID") @PathVariable Long id) {
        postService.deletePost(id);
        return ApiResponse.success("Post deleted");
    }
}
