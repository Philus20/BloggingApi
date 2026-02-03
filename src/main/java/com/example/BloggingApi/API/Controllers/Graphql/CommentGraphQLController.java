package com.example.BloggingApi.API.Controllers.Graphql;

import com.example.BloggingApi.API.Requests.CreateCommentRequest;
import com.example.BloggingApi.API.Requests.EditCommentRequest;
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
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CommentGraphQLController {

    private final CreateComment createCommentHandler;
    private final EditComment editCommentHandler;
    private final DeleteComment deleteCommentHandler;
    private final GetCommentById getCommentByIdHandler;
    private final GetAllComments getAllCommentsHandler;
    private final SearchComments searchCommentsHandler;

    public CommentGraphQLController(CreateComment createCommentHandler, EditComment editCommentHandler, DeleteComment deleteCommentHandler, GetCommentById getCommentByIdHandler, GetAllComments getAllCommentsHandler, SearchComments searchCommentsHandler) {
        this.createCommentHandler = createCommentHandler;
        this.editCommentHandler = editCommentHandler;
        this.deleteCommentHandler = deleteCommentHandler;
        this.getCommentByIdHandler = getCommentByIdHandler;
        this.getAllCommentsHandler = getAllCommentsHandler;
        this.searchCommentsHandler = searchCommentsHandler;
    }

    @QueryMapping
    public Comment getComment(@Argument Long id) {
        return getCommentByIdHandler.handle(id);
    }

    @QueryMapping
    public Page<Comment> listComments(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        System.out.println("*************************************************************");
        System.out.println(pageable);
        System.out.println("*************************************************************");

        return getAllCommentsHandler.handle(pageable);
    }

    @QueryMapping
    public Page<Comment> searchComments(@Argument String content, @Argument String author,
                                        @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (content != null && !content.isBlank()) {
            return searchCommentsHandler.searchByContent(content, pageable);
        } else if (author != null && !author.isBlank()) {
            return searchCommentsHandler.searchByAuthor(author, pageable);
        }
        return Page.empty(pageable);
    }

    @MutationMapping
    public Comment createComment(@Argument String content, @Argument Long postId, @Argument Long authorId) {
        CreateCommentRequest request = new CreateCommentRequest(content, postId, authorId);
        return createCommentHandler.handle(request);
    }

    @MutationMapping
    public Comment editComment(@Argument Long id, @Argument String content) {
        EditCommentRequest request = new EditCommentRequest(id, content);
        return editCommentHandler.handle(request);
    }

    @MutationMapping
    public String deleteComment(@Argument Long id) {
        deleteCommentHandler.handle(id);
        return "Comment with ID " + id + " deleted successfully.";
    }
}
