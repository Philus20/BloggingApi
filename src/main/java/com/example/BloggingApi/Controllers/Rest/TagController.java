package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Requests.CreateTagRequest;
import com.example.BloggingApi.DTOs.Requests.EditTagRequest;
import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.TagResponse;
import com.example.BloggingApi.Services.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Tags", description = "Tag CRUD and search")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags/{id}")
    @Operation(summary = "Get tag by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ApiResponse<TagResponse> getTagById(@Parameter(description = "Tag ID") @PathVariable Long id) {
        return ApiResponse.success("Tag retrieved successfully", TagResponse.from(tagService.getById(id)));
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all tags", description = "Paginated list with optional sorting")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tags retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameter")
    })
    public ApiResponse<Page<TagResponse>> getAllTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success("Tags retrieved successfully", tagService.getAll(page, size, sortBy, ascending).map(TagResponse::from));
    }

    @GetMapping("/tags/search")
    @Operation(summary = "Search tags by name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing required 'name' parameter")
    })
    public ApiResponse<Page<TagResponse>> searchTags(
            @RequestParam(required = true) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success("Tags search completed successfully", tagService.searchByName(name, page, size, sortBy, ascending).map(TagResponse::from));
    }

    @PostMapping("/tags")
    @Operation(summary = "Create a new tag")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (blank name)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tag name already exists")
    })
    public ApiResponse<TagResponse> createTag(@RequestBody @jakarta.validation.Valid CreateTagRequest request) {
        return ApiResponse.success("Tag created successfully", TagResponse.from(tagService.create(request)));
    }

    @PutMapping("/tags")
    @Operation(summary = "Update a tag")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tag name already taken")
    })
    public ApiResponse<TagResponse> editTag(@RequestBody @jakarta.validation.Valid EditTagRequest request) {
        return ApiResponse.success("Tag updated successfully", TagResponse.from(tagService.update(request)));
    }

    @DeleteMapping("/tags/{id}")
    @Operation(summary = "Delete a tag")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ApiResponse<Void> deleteTag(@Parameter(description = "Tag ID") @PathVariable Long id) {
        tagService.delete(id);
        return ApiResponse.success("Tag deleted successfully");
    }
}
