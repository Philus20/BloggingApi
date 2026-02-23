package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Requests.CreateCommentRequest;
import com.example.BloggingApi.DTOs.Requests.EditCommentRequest;
import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.CommentResponse;
import com.example.BloggingApi.Services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Comments", description = "Comment CRUD and search")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/comments/{id}")
    @Operation(summary = "Get comment by ID")
    public ApiResponse<CommentResponse> getCommentById(@Parameter(description = "Comment ID") @PathVariable Long id) {
        return ApiResponse.success("Comment retrieved successfully", CommentResponse.from(commentService.getById(id)));
    }

    @GetMapping("/comments")
    @Operation(summary = "Get all comments", description = "Paginated list with optional sorting")
    public ApiResponse<Page<CommentResponse>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success("Comments retrieved successfully", commentService.getAll(page, size, sortBy, ascending).map(CommentResponse::from));
    }

    @GetMapping("/comments/search")
    @Operation(summary = "Search comments", description = "Search by content or author")
    public ApiResponse<Page<CommentResponse>> searchComments(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) {
        return ApiResponse.success("Comments search completed successfully", commentService.search(content, author, page, size, sortBy, ascending).map(CommentResponse::from));
    }

    @PostMapping("/comments")
    @Operation(summary = "Create a new comment")
    public ApiResponse<CommentResponse> createComment(@RequestBody @jakarta.validation.Valid CreateCommentRequest request) {
        return ApiResponse.success("Comment created successfully", CommentResponse.from(commentService.create(request)));
    }

    @PutMapping("/comments")
    @Operation(summary = "Update a comment")
    public ApiResponse<CommentResponse> editComment(@RequestBody @jakarta.validation.Valid EditCommentRequest request) {
        return ApiResponse.success("Comment updated successfully", CommentResponse.from(commentService.update(request)));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "Delete a comment")
    public ApiResponse<Void> deleteComment(@Parameter(description = "Comment ID") @PathVariable Long id) {
        commentService.delete(id);
        return ApiResponse.success("Comment deleted successfully");
    }
}
