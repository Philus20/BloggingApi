package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Requests.CreatePostRequest;
import com.example.BloggingApi.DTOs.Requests.EditPostRequest;
import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.PostsResponse;
import com.example.BloggingApi.Services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Posts", description = "Blog post CRUD and search")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts/{id}")
    @Operation(summary = "Get post by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ApiResponse<PostsResponse> getPostById(@Parameter(description = "Post ID") @PathVariable Long id) {
        return ApiResponse.success("Post retrieved successfully", PostsResponse.from(postService.getById(id)));
    }

    @GetMapping("/posts")
    @Operation(summary = "Get all posts", description = "Paginated list with optional sorting")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Posts retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameter")
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No search parameter provided")
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (blank title, content, etc.)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Author not found")
    })
    public ApiResponse<PostsResponse> createPost(@RequestBody @jakarta.validation.Valid CreatePostRequest request) {
        return ApiResponse.success("Success", PostsResponse.from(postService.create(request)));
    }

    @PutMapping("/posts")
    @Operation(summary = "Update a post")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ApiResponse<PostsResponse> editPost(@RequestBody @jakarta.validation.Valid EditPostRequest request) {
        return ApiResponse.success("Success", PostsResponse.from(postService.update(request)));
    }

    @DeleteMapping("/posts/{id}")
    @Operation(summary = "Delete a post")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ApiResponse<Void> deletePost(@Parameter(description = "Post ID") @PathVariable Long id) {
        postService.delete(id);
        return ApiResponse.success("Post deleted");
    }

}
