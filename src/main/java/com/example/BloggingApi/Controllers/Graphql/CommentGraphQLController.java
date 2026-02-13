package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.RequestsDTO.CreateCommentRequest;
import com.example.BloggingApi.RequestsDTO.EditCommentRequest;
import com.example.BloggingApi.ResposesDTO.CommentResponse;
import com.example.BloggingApi.Services.CommentServices;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CommentGraphQLController {

    private final CommentServices commentServices;

    public CommentGraphQLController(CommentServices commentServices) {
        this.commentServices = commentServices;
    }

    @QueryMapping
    public CommentResponse getComment(@Argument Long id) {
        return commentServices.getCommentById(id);
    }

    @QueryMapping
    public Page<CommentResponse> listComments(
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return commentServices.getAllComments(page, size, sortBy, ascending);
    }

    @QueryMapping
    public Page<CommentResponse> searchComments(
            @Argument String content,
            @Argument String author,
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return commentServices.searchComments(content, author, page, size, sortBy, ascending);
    }

    @MutationMapping
    public CommentResponse createComment(
            @Argument String content,
            @Argument Long postId,
            @Argument Long authorId
    ) {
        CreateCommentRequest request = new CreateCommentRequest(content, postId, authorId);
        return commentServices.createComment(request);
    }

    @MutationMapping
    public CommentResponse editComment(
            @Argument Long id,
            @Argument String content
    ) {
        EditCommentRequest request = new EditCommentRequest(id, content);
        return commentServices.editComment(request);
    }

    @MutationMapping
    public String deleteComment(@Argument Long id) {
        commentServices.deleteComment(id);
        return "Comment with ID " + id + " deleted successfully.";
    }
}
