package com.example.BloggingApi.API.Controllers;


import com.example.BloggingApi.API.Requests.CreatePostRequest;
import com.example.BloggingApi.API.Requests.EditPostRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.API.Resposes.PostsResponse;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreatePost;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeletePost;
import com.example.BloggingApi.Application.Commands.EditCommands.EditPost;
import com.example.BloggingApi.Application.Queries.GetAllPosts;
import com.example.BloggingApi.Application.Queries.GetPostById;
import com.example.BloggingApi.Domain.Entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PostController {

    private final CreatePost createPostHandler;
    private final EditPost editPostHandler;
    private final DeletePost deletePostHandler;
    private final GetPostById getPostByIdHandler;
    private final GetAllPosts getAllPostsHandler;

    public PostController(CreatePost createPostHandler, EditPost editPostHandler, DeletePost deletePostHandler, GetPostById getPostByIdHandler, GetAllPosts getAllPostsHandler) {
        this.deletePostHandler = deletePostHandler;
        this.editPostHandler = editPostHandler;
        this.createPostHandler = createPostHandler;
        this.getPostByIdHandler = getPostByIdHandler;
        this.getAllPostsHandler = getAllPostsHandler;
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<Post> getPostById(@PathVariable Long id) {
        try {
            Post post = getPostByIdHandler.handle(id);
            return ApiResponse.success("Post retrieved successfully", post);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @GetMapping("/posts")
    public ApiResponse<Page<PostsResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        try {
            Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Post> postsPage = getAllPostsHandler.handle(pageable);
            Page<PostsResponse> response =
                    postsPage.map(PostsResponse::from);
            return ApiResponse.success("Posts retrieved successfully", response);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }


    @PostMapping("/posts")
    public ApiResponse<Post> createPost(@RequestBody CreatePostRequest request) {

        try {
            Post createdPost = createPostHandler.handle(request);
            return ApiResponse.success("Success", createdPost);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }


    }

    @PutMapping("/posts")
    public ApiResponse<Post> editPost(@RequestBody EditPostRequest request) {

        try {
            Post updatedPost = editPostHandler.handle(request);
            return ApiResponse.success("Success", updatedPost);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        try {
            deletePostHandler.handle(id);
            return ApiResponse.success("Post deleted");
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

}