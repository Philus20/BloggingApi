package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.Entities.Tag;
import com.example.BloggingApi.RequestsDTO.CreateTagRequest;
import com.example.BloggingApi.RequestsDTO.EditTagRequest;
import com.example.BloggingApi.ResposesDTO.TagResponse;
import com.example.BloggingApi.Services.TagService;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class TagGraphQLController {

    private final TagService tagService;

    public TagGraphQLController(TagService tagService) {
        this.tagService = tagService;
    }

    @QueryMapping
    public Tag getTag(@Argument Long id) {
        return tagService.getTagById(id);
    }

    @QueryMapping
    public Page<TagResponse> listTags(
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return tagService.getAllTags(page, size, sortBy, ascending);
    }

    @QueryMapping
    public Page<TagResponse> searchTags(
            @Argument String name,
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return tagService.searchTags(name, page, size, sortBy, ascending);
    }

    @MutationMapping
    public Tag createTag(@Argument String name) {
        CreateTagRequest request = new CreateTagRequest(name);
        return tagService.createTag(request);
    }

    @MutationMapping
    public Tag editTag(@Argument Long id, @Argument String name) {
        EditTagRequest request = new EditTagRequest(id, name);
        return tagService.editTag(request);
    }

    @MutationMapping
    public String deleteTag(@Argument Long id) {
        tagService.deleteTag(id);
        return "Tag with ID " + id + " deleted successfully.";
    }
}
