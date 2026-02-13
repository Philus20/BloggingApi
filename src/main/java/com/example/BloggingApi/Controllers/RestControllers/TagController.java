package com.example.BloggingApi.Controllers.RestControllers;

import com.example.BloggingApi.RequestsDTO.CreateTagRequest;
import com.example.BloggingApi.RequestsDTO.EditTagRequest;
import com.example.BloggingApi.ResposesDTO.ApiResponse;
import com.example.BloggingApi.ResposesDTO.TagResponse;
import com.example.BloggingApi.Services.TagService;
import com.example.BloggingApi.Entities.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Create, read, update, and delete tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }


    @Operation(summary = "Get tag by ID")
    @GetMapping("/tags/{id}")
    public ApiResponse<TagResponse> getTagById(@Parameter(description = "Tag ID") @PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        return ApiResponse.success("Tag retrieved successfully", TagResponse.from(tag));
    }

    @Operation(summary = "List all tags", description = "Paginated and sorted list of tags")
    @GetMapping("/tags")
    public ApiResponse<Page<TagResponse>> getAllTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success(
                "Tags retrieved successfully",
                tagService.getAllTags(page, size, sortBy, ascending)
        );
    }


    @Operation(summary = "Search tags by name")
    @GetMapping("/tags/search")
    public ApiResponse<Page<TagResponse>> searchTags(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success(
                "Tags search completed successfully",
                tagService.searchTags(name, page, size, sortBy, ascending)
        );
    }


    @Operation(summary = "Create tag", description = "Requires name")
    @PostMapping("/tags")
    public ApiResponse<TagResponse> createTag(@RequestBody @jakarta.validation.Valid CreateTagRequest request) {
        Tag tag = tagService.createTag(request);
        return ApiResponse.success("Tag created successfully", TagResponse.from(tag));
    }

    @Operation(summary = "Update tag")
    @PutMapping("/tags")
    public ApiResponse<TagResponse> editTag(@RequestBody @jakarta.validation.Valid EditTagRequest request) {
        Tag tag = tagService.editTag(request);
        return ApiResponse.success("Tag updated successfully", TagResponse.from(tag));
    }

    @Operation(summary = "Delete tag")
    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@Parameter(description = "Tag ID") @PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.success("Tag deleted successfully");
    }
}
