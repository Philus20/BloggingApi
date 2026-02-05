package com.example.BloggingApi.API.Controllers.RestControllers;


import com.example.BloggingApi.API.Requests.CreatePostRequest;
import com.example.BloggingApi.API.Requests.EditPostRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.API.Resposes.PostsResponse;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreatePost;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeletePost;
import com.example.BloggingApi.Application.Commands.EditCommands.EditPost;
import com.example.BloggingApi.Application.Queries.GetAllPosts;
import com.example.BloggingApi.Application.Queries.GetPostById;
import com.example.BloggingApi.Application.Queries.SearchPosts;
import com.example.BloggingApi.Domain.Entities.Post;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {

    private final CreatePost createPostHandler;
    private final EditPost editPostHandler;
    private final DeletePost deletePostHandler;
    private final GetPostById getPostByIdHandler;
    private final GetAllPosts getAllPostsHandler;
    private final SearchPosts searchPostsHandler;

    public PostController(CreatePost createPostHandler, EditPost editPostHandler, DeletePost deletePostHandler, GetPostById getPostByIdHandler, GetAllPosts getAllPostsHandler, SearchPosts searchPostsHandler) {
        this.deletePostHandler = deletePostHandler;
        this.editPostHandler = editPostHandler;
        this.createPostHandler = createPostHandler;
        this.getPostByIdHandler = getPostByIdHandler;
        this.getAllPostsHandler = getAllPostsHandler;
        this.searchPostsHandler = searchPostsHandler;
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<PostsResponse> getPostById(@PathVariable Long id) {
        Post post = getPostByIdHandler.handle(id);
        return ApiResponse.success("Post retrieved successfully", PostsResponse.from(post));
    }

    @GetMapping("/posts")
    public ApiResponse<Page<PostsResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> postsPage = getAllPostsHandler.handle(pageable);
        Page<PostsResponse> response =
                postsPage.map(PostsResponse::from);
        return ApiResponse.success("Posts retrieved successfully", response);
    }

    @GetMapping("/posts/search")
    public ApiResponse<Page<PostsResponse>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) {
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> postsPage;

        if (keyword != null && !keyword.isBlank()) {
            postsPage = searchPostsHandler.searchByKeyword(keyword, pageable);
        } else if (title != null && !title.isBlank()) {
            postsPage = searchPostsHandler.searchByTitle(title, pageable);
        } else if (author != null && !author.isBlank()) {
            postsPage = searchPostsHandler.searchByAuthor(author, pageable);
        } else {
            throw new IllegalArgumentException("Please provide at least one search parameter: keyword, title, or author");
        }

        Page<PostsResponse> response = postsPage.map(PostsResponse::from);
        return ApiResponse.success("Posts search completed successfully", response);
    }

    @PostMapping("/posts")
    public ApiResponse<PostsResponse> createPost(@RequestBody @Valid CreatePostRequest request) {
        Post createdPost = createPostHandler.handle(request);
        return ApiResponse.success("Success", PostsResponse.from(createdPost));
    }

    @PutMapping("/posts")
    public ApiResponse<PostsResponse> editPost(@RequestBody @Valid EditPostRequest request) {
        Post updatedPost = editPostHandler.handle(request);
        return ApiResponse.success("Success", PostsResponse.from(updatedPost));
    }

    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        deletePostHandler.handle(id);
        return ApiResponse.success("Post deleted");
    }

}
