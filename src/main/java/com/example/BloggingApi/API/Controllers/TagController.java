package com.example.BloggingApi.API.Controllers;

import com.example.BloggingApi.API.Requests.CreateTagRequest;
import com.example.BloggingApi.API.Requests.EditTagRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.API.Resposes.TagResponse;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreateTag;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeleteTag;
import com.example.BloggingApi.Application.Commands.EditCommands.EditTag;
import com.example.BloggingApi.Application.Queries.GetAllTags;
import com.example.BloggingApi.Application.Queries.GetTagById;
import com.example.BloggingApi.Domain.Entities.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
public class TagController {

    private final CreateTag createTagHandler;
    private final EditTag editTagHandler;
    private final DeleteTag deleteTagHandler;
    private final GetTagById getTagByIdHandler;
    private final GetAllTags getAllTagsHandler;

    public TagController(CreateTag createTagHandler, EditTag editTagHandler, DeleteTag deleteTagHandler, GetTagById getTagByIdHandler, GetAllTags getAllTagsHandler) {
        this.createTagHandler = createTagHandler;
        this.editTagHandler = editTagHandler;
        this.deleteTagHandler = deleteTagHandler;
        this.getTagByIdHandler = getTagByIdHandler;
        this.getAllTagsHandler = getAllTagsHandler;
    }

    @GetMapping("/tags/{id}")
    public ApiResponse<Tag> getTagById(@PathVariable Long id) {
        try {
            Tag tag = getTagByIdHandler.handle(id);
            return ApiResponse.success("Tag retrieved successfully", tag);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @GetMapping("/tags")
    public ApiResponse<Page<TagResponse>> getAllTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        try {
            Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Tag> tagsPage = getAllTagsHandler.handle(pageable);
            Page<TagResponse> response = tagsPage.map(TagResponse::from);
            return ApiResponse.success("Tags retrieved successfully", response);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @PostMapping("/tags")
    public ApiResponse<Tag> createTag(@RequestBody CreateTagRequest request) {
        try {
            Tag tag = createTagHandler.handle(request);
            return ApiResponse.success("Tag created successfully", tag);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @PutMapping("/tags")
    public ApiResponse<Tag> editTag(@RequestBody EditTagRequest request) {
        try {
            Tag tag = editTagHandler.handle(request);
            return ApiResponse.success("Tag updated successfully", tag);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        try {
            deleteTagHandler.handle(id);
            return ApiResponse.success("Tag deleted successfully");
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }
}
