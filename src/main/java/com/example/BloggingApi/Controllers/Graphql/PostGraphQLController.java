package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.DTOs.Requests.CreatePostRequest;
import com.example.BloggingApi.DTOs.Requests.EditPostRequest;
import com.example.BloggingApi.Services.PostService;
import com.example.BloggingApi.Domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PostGraphQLController {

    private final PostService postService;

    public PostGraphQLController(PostService postService) {
        this.postService = postService;
    }

    @QueryMapping
    public Post getPost(@Argument Long id) {
        return postService.getById(id);
    }

    @QueryMapping
    public Page<Post> listPosts(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return postService.getAll(page, size, sortBy, ascending);
    }

    @QueryMapping
    public Page<Post> searchPosts(@Argument String keyword, @Argument String title, @Argument String author,
                                  @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return postService.searchOptional(keyword, title, author, page, size, sortBy, ascending);
    }

    @MutationMapping
    public Post createPost(@Argument String title, @Argument String content, @Argument Long authorId) {
        return postService.create(new CreatePostRequest(title, content, authorId));
    }

    @MutationMapping
    public Post editPost(@Argument Long id, @Argument String title, @Argument String content) {
        return postService.update(new EditPostRequest(id, title, content));
    }

    @MutationMapping
    public String deletePost(@Argument Long id) {
        postService.delete(id);
        return "Post with ID " + id + " deleted successfully.";
    }
}
