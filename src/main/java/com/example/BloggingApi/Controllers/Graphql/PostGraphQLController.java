package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.RequestsDTO.CreatePostRequest;
import com.example.BloggingApi.RequestsDTO.EditPostRequest;
import com.example.BloggingApi.ResposesDTO.PostsResponse;
import com.example.BloggingApi.Services.PostService;
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
    public PostsResponse getPost(@Argument Long id) {
        return postService.getPostById(id);
    }

    @QueryMapping
    public Page<PostsResponse> listPosts(
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return postService.getAllPosts(page, size, sortBy, ascending);
    }

    @QueryMapping
    public Page<PostsResponse> searchPosts(
            @Argument String keyword,
            @Argument String title,
            @Argument String author,
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return postService.searchPosts(keyword, title, author, page, size, sortBy, ascending);
    }

    @MutationMapping
    public PostsResponse createPost(
            @Argument String title,
            @Argument String content,
            @Argument Long authorId
    ) {
        CreatePostRequest request = new CreatePostRequest(title, content, authorId);
        return postService.createPost(request);
    }

    @MutationMapping
    public PostsResponse editPost(
            @Argument Long id,
            @Argument String title,
            @Argument String content
    ) {
        EditPostRequest request = new EditPostRequest(id, title, content);
        return postService.editPost(request);
    }

    @MutationMapping
    public String deletePost(@Argument Long id) {
        postService.deletePost(id);
        return "Post with ID " + id + " deleted successfully.";
    }
}
