package com.example.BloggingApi.API.Controllers;

import com.example.BloggingApi.API.Requests.CreateCommentRequest;
import com.example.BloggingApi.API.Requests.EditCommentRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.API.Resposes.CommentResponse;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreateComment;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeleteComment;
import com.example.BloggingApi.Application.Commands.EditCommands.EditComment;
import com.example.BloggingApi.Application.Queries.GetAllComments;
import com.example.BloggingApi.Application.Queries.GetCommentById;
import com.example.BloggingApi.Domain.Entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {

    private final CreateComment createCommentHandler;
    private final EditComment editCommentHandler;
    private final DeleteComment deleteCommentHandler;
    private final GetCommentById getCommentByIdHandler;
    private final GetAllComments getAllCommentsHandler;

    public CommentController(CreateComment createCommentHandler, EditComment editCommentHandler, DeleteComment deleteCommentHandler, GetCommentById getCommentByIdHandler, GetAllComments getAllCommentsHandler) {
        this.createCommentHandler = createCommentHandler;
        this.editCommentHandler = editCommentHandler;
        this.deleteCommentHandler = deleteCommentHandler;
        this.getCommentByIdHandler = getCommentByIdHandler;
        this.getAllCommentsHandler = getAllCommentsHandler;
    }

    @GetMapping("/comments/{id}")
    public ApiResponse<Comment> getCommentById(@PathVariable Long id) {
        try {
            Comment comment = getCommentByIdHandler.handle(id);
            return ApiResponse.success("Comment retrieved successfully", comment);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @GetMapping("/comments")
    public ApiResponse<Page<CommentResponse>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        try {
            Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Comment> commentsPage = getAllCommentsHandler.handle(pageable);
            Page<CommentResponse> response = commentsPage.map(CommentResponse::from);
            return ApiResponse.success("Comments retrieved successfully", response);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @PostMapping("/comments")
    public ApiResponse<Comment> createComment(@RequestBody CreateCommentRequest request) {
        try {
            Comment comment = createCommentHandler.handle(request);
            return ApiResponse.success("Comment created successfully", comment);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @PutMapping("/comments")
    public ApiResponse<Comment> editComment(@RequestBody EditCommentRequest request) {
        try {
            Comment comment = editCommentHandler.handle(request);
            return ApiResponse.success("Comment updated successfully", comment);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @DeleteMapping("/comments/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        try {
            deleteCommentHandler.handle(id);
            return ApiResponse.success("Comment deleted successfully");
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }
}
