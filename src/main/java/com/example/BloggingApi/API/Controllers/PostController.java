package com.example.BloggingApi.API.Controllers;


import com.example.BloggingApi.API.Requests.CreatePostRequest;
import com.example.BloggingApi.API.Requests.EditPostRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreatePost;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeletePost;
import com.example.BloggingApi.Application.Commands.EditCommands.EditPost;
import com.example.BloggingApi.Domain.Entities.Post;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {

    private final CreatePost createPostHandler;
    private final EditPost editPostHandler;
    private final DeletePost deletePostHandler;

    public PostController(CreatePost createPostHandler, EditPost editPostHandler, DeletePost deletePostHandler) {
        this.deletePostHandler = deletePostHandler;
        this.editPostHandler = editPostHandler;
        this.createPostHandler = createPostHandler;
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