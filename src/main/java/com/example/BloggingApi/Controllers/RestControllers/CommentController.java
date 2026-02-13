package com.example.BloggingApi.Controllers.RestControllers;

import com.example.BloggingApi.RequestsDTO.CreateCommentRequest;
import com.example.BloggingApi.RequestsDTO.EditCommentRequest;
import com.example.BloggingApi.ResposesDTO.ApiResponse;
import com.example.BloggingApi.ResposesDTO.CommentResponse;
import com.example.BloggingApi.Services.CommentServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comments", description = "Create, read, update, and delete comments on posts")
public class CommentController {

    private final CommentServices commentServices;

    public CommentController(CommentServices commentServices) {
        this.commentServices = commentServices;
    }

    @Operation(summary = "Get comment by ID")
    @GetMapping("/{id}")
    public ApiResponse<CommentResponse> getCommentById(@Parameter(description = "Comment ID") @PathVariable Long id) {
        return ApiResponse.success(
                "Comment retrieved successfully",
                commentServices.getCommentById(id)
        );
    }

    @Operation(summary = "List all comments", description = "Paginated and sorted list of comments")
    @GetMapping
    public ApiResponse<Page<CommentResponse>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success(
                "Comments retrieved successfully",
                commentServices.getAllComments(page, size, sortBy, ascending)
        );
    }

    @Operation(summary = "Search comments", description = "Search by content or author. At least one parameter required.")
    @GetMapping("/search")
    public ApiResponse<Page<CommentResponse>> searchComments(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) {
        return ApiResponse.success(
                "Comments search completed successfully",
                commentServices.searchComments(content, author, page, size, sortBy, ascending)
        );
    }

    @Operation(summary = "Create comment", description = "Requires content, postId, and authorId")
    @PostMapping
    public ApiResponse<CommentResponse> createComment(@RequestBody @Valid CreateCommentRequest request) {
        return ApiResponse.success(
                "Comment created successfully",
                commentServices.createComment(request)
        );
    }

    @Operation(summary = "Update comment")
    @PutMapping
    public ApiResponse<CommentResponse> editComment(@RequestBody @Valid EditCommentRequest request) {
        return ApiResponse.success(
                "Comment updated successfully",
                commentServices.editComment(request)
        );
    }

    @Operation(summary = "Delete comment")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@Parameter(description = "Comment ID") @PathVariable Long id) {
        commentServices.deleteComment(id);
        return ApiResponse.success("Comment deleted successfully");
    }
}
