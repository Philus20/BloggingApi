package com.example.BloggingApi.API.Controllers.RestControllers;

import com.example.BloggingApi.API.Requests.CreateTagRequest;
import com.example.BloggingApi.API.Requests.EditTagRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.API.Resposes.TagResponse;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreateTag;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeleteTag;
import com.example.BloggingApi.Application.Commands.EditCommands.EditTag;
import com.example.BloggingApi.Application.Queries.GetAllTags;
import com.example.BloggingApi.Application.Queries.GetTagById;
import com.example.BloggingApi.Application.Queries.SearchTags;
import com.example.BloggingApi.Domain.Entities.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")

public class TagController {

    private final CreateTag createTagHandler;
    private final EditTag editTagHandler;
    private final DeleteTag deleteTagHandler;
    private final GetTagById getTagByIdHandler;
    private final GetAllTags getAllTagsHandler;
    private final SearchTags searchTagsHandler;

    public TagController(CreateTag createTagHandler, EditTag editTagHandler, DeleteTag deleteTagHandler, GetTagById getTagByIdHandler, GetAllTags getAllTagsHandler, SearchTags searchTagsHandler) {
        this.createTagHandler = createTagHandler;
        this.editTagHandler = editTagHandler;
        this.deleteTagHandler = deleteTagHandler;
        this.getTagByIdHandler = getTagByIdHandler;
        this.getAllTagsHandler = getAllTagsHandler;
        this.searchTagsHandler = searchTagsHandler;
    }

    @GetMapping("/tags/{id}")
    public ApiResponse<TagResponse> getTagById(@PathVariable Long id) {
        Tag tag = getTagByIdHandler.handle(id);
        return ApiResponse.success("Tag retrieved successfully", TagResponse.from(tag));
    }

    @GetMapping("/tags")
    public ApiResponse<Page<TagResponse>> getAllTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Tag> tagsPage = getAllTagsHandler.handle(pageable);
        Page<TagResponse> response = tagsPage.map(TagResponse::from);
        return ApiResponse.success("Tags retrieved successfully", response);
    }

    @GetMapping("/tags/search")
    public ApiResponse<Page<TagResponse>> searchTags(
            @RequestParam(required = true) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Tag> tagsPage = searchTagsHandler.searchByName(name, pageable);
        Page<TagResponse> response = tagsPage.map(TagResponse::from);
        return ApiResponse.success("Tags search completed successfully", response);
    }

    @PostMapping("/tags")
    public ApiResponse<TagResponse> createTag(@RequestBody @jakarta.validation.Valid CreateTagRequest request) {
        Tag tag = createTagHandler.handle(request);
        return ApiResponse.success("Tag created successfully", TagResponse.from(tag));
    }

    @PutMapping("/tags")
    public ApiResponse<TagResponse> editTag(@RequestBody @jakarta.validation.Valid EditTagRequest request) {
        Tag tag = editTagHandler.handle(request);
        return ApiResponse.success("Tag updated successfully", TagResponse.from(tag));
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        deleteTagHandler.handle(id);
        return ApiResponse.success("Tag deleted successfully");
    }
}
