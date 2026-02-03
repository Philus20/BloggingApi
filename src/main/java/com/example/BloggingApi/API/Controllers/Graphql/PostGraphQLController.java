package com.example.BloggingApi.API.Controllers.Graphql;

import com.example.BloggingApi.API.Requests.CreatePostRequest;
import com.example.BloggingApi.API.Requests.EditPostRequest;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreatePost;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeletePost;
import com.example.BloggingApi.Application.Commands.EditCommands.EditPost;
import com.example.BloggingApi.Application.Queries.GetAllPosts;
import com.example.BloggingApi.Application.Queries.GetPostById;
import com.example.BloggingApi.Application.Queries.SearchPosts;
import com.example.BloggingApi.Domain.Entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PostGraphQLController {

    private final CreatePost createPostHandler;
    private final EditPost editPostHandler;
    private final DeletePost deletePostHandler;
    private final GetPostById getPostByIdHandler;
    private final GetAllPosts getAllPostsHandler;
    private final SearchPosts searchPostsHandler;

    public PostGraphQLController(CreatePost createPostHandler, EditPost editPostHandler, DeletePost deletePostHandler, GetPostById getPostByIdHandler, GetAllPosts getAllPostsHandler, SearchPosts searchPostsHandler) {
        this.createPostHandler = createPostHandler;
        this.editPostHandler = editPostHandler;
        this.deletePostHandler = deletePostHandler;
        this.getPostByIdHandler = getPostByIdHandler;
        this.getAllPostsHandler = getAllPostsHandler;
        this.searchPostsHandler = searchPostsHandler;
    }

    @QueryMapping
    public Post getPost(@Argument Long id) {
        return getPostByIdHandler.handle(id);
    }

    @QueryMapping
    public Page<Post> listPosts(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return getAllPostsHandler.handle(pageable);
    }

    @QueryMapping
    public Page<Post> searchPosts(@Argument String keyword, @Argument String title, @Argument String author,
                                  @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword != null && !keyword.isBlank()) {
            return searchPostsHandler.searchByKeyword(keyword, pageable);
        } else if (title != null && !title.isBlank()) {
            return searchPostsHandler.searchByTitle(title, pageable);
        } else if (author != null && !author.isBlank()) {
            return searchPostsHandler.searchByAuthor(author, pageable);
        }
        return Page.empty(pageable);
    }

    @MutationMapping
    public Post createPost(@Argument String title, @Argument String content, @Argument Long authorId) {
        CreatePostRequest request = new CreatePostRequest(title, content, authorId);
        return createPostHandler.handle(request);
    }

    @MutationMapping
    public Post editPost(@Argument Long id, @Argument String title, @Argument String content) {
        EditPostRequest request = new EditPostRequest(id, title, content);
        return editPostHandler.handle(request);
    }

    @MutationMapping
    public String deletePost(@Argument Long id) {
        deletePostHandler.handle(id);
        return "Post with ID " + id + " deleted successfully.";
    }
}
