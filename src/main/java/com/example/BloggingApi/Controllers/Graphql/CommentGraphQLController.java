package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.DTOs.Requests.CreateCommentRequest;
import com.example.BloggingApi.DTOs.Requests.EditCommentRequest;
import com.example.BloggingApi.Services.CommentService;
import com.example.BloggingApi.Domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CommentGraphQLController {

    private final CommentService commentService;

    public CommentGraphQLController(CommentService commentService) {
        this.commentService = commentService;
    }

    @QueryMapping
    public Comment getComment(@Argument Long id) {
        return commentService.getById(id);
    }

    @QueryMapping
    public Page<Comment> listComments(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return commentService.getAll(page, size, sortBy, ascending);
    }

    @QueryMapping
    public Page<Comment> searchComments(@Argument String content, @Argument String author,
                                        @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return commentService.searchOptional(content, author, page, size, sortBy, ascending);
    }

    @MutationMapping
    public Comment createComment(@Argument String content, @Argument Long postId, @Argument Long authorId) {
        return commentService.create(new CreateCommentRequest(content, postId, authorId));
    }

    @MutationMapping
    public Comment editComment(@Argument Long id, @Argument String content) {
        return commentService.update(new EditCommentRequest(id, content));
    }

    @MutationMapping
    public String deleteComment(@Argument Long id) {
        commentService.delete(id);
        return "Comment with ID " + id + " deleted successfully.";
    }
}
