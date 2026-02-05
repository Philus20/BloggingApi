package com.example.BloggingApi.API.Controllers.RestControllers;

import com.example.BloggingApi.API.Requests.CreateCommentRequest;
import com.example.BloggingApi.API.Requests.EditCommentRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.API.Resposes.CommentResponse;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreateComment;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeleteComment;
import com.example.BloggingApi.Application.Commands.EditCommands.EditComment;
import com.example.BloggingApi.Application.Queries.GetAllComments;
import com.example.BloggingApi.Application.Queries.GetCommentById;
import com.example.BloggingApi.Application.Queries.SearchComments;
import com.example.BloggingApi.Domain.Entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class CommentController {

    private final CreateComment createCommentHandler;
    private final EditComment editCommentHandler;
    private final DeleteComment deleteCommentHandler;
    private final GetCommentById getCommentByIdHandler;
    private final GetAllComments getAllCommentsHandler;
    private final SearchComments searchCommentsHandler;

    public CommentController(CreateComment createCommentHandler, EditComment editCommentHandler, DeleteComment deleteCommentHandler, GetCommentById getCommentByIdHandler, GetAllComments getAllCommentsHandler, SearchComments searchCommentsHandler) {
        this.createCommentHandler = createCommentHandler;
        this.editCommentHandler = editCommentHandler;
        this.deleteCommentHandler = deleteCommentHandler;
        this.getCommentByIdHandler = getCommentByIdHandler;
        this.getAllCommentsHandler = getAllCommentsHandler;
        this.searchCommentsHandler = searchCommentsHandler;
    }

    @GetMapping("/comments/{id}")
    public ApiResponse<CommentResponse> getCommentById(@PathVariable Long id) {
        Comment comment = getCommentByIdHandler.handle(id);
        return ApiResponse.success("Comment retrieved successfully", CommentResponse.from(comment));
    }

    @GetMapping("/comments")
    public ApiResponse<Page<CommentResponse>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comment> commentsPage = getAllCommentsHandler.handle(pageable);
        Page<CommentResponse> response = commentsPage.map(CommentResponse::from);
        return ApiResponse.success("Comments retrieved successfully", response);
    }

    @GetMapping("/comments/search")
    public ApiResponse<Page<CommentResponse>> searchComments(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) {
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comment> commentsPage;

        if (content != null && !content.isBlank()) {
            commentsPage = searchCommentsHandler.searchByContent(content, pageable);
        } else if (author != null && !author.isBlank()) {
            commentsPage = searchCommentsHandler.searchByAuthor(author, pageable);
        } else {
            throw new IllegalArgumentException("Please provide at least one search parameter: content or author");
        }

        Page<CommentResponse> response = commentsPage.map(CommentResponse::from);
        return ApiResponse.success("Comments search completed successfully", response);
    }

    @PostMapping("/comments")
    public ApiResponse<CommentResponse> createComment(@RequestBody @jakarta.validation.Valid CreateCommentRequest request) {
        Comment comment = createCommentHandler.handle(request);
        return ApiResponse.success("Comment created successfully", CommentResponse.from(comment));
    }

    @PutMapping("/comments")
    public ApiResponse<CommentResponse> editComment(@RequestBody @jakarta.validation.Valid EditCommentRequest request) {
        Comment comment = editCommentHandler.handle(request);
        return ApiResponse.success("Comment updated successfully", CommentResponse.from(comment));
    }

    @DeleteMapping("/comments/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        deleteCommentHandler.handle(id);
        return ApiResponse.success("Comment deleted successfully");
    }
}
