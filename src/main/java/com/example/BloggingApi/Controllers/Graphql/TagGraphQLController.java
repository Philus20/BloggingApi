package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.DTOs.Requests.CreateTagRequest;
import com.example.BloggingApi.DTOs.Requests.EditTagRequest;
import com.example.BloggingApi.Services.TagService;
import com.example.BloggingApi.Domain.Tag;
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
        return tagService.getById(id);
    }

    @QueryMapping
    public Page<Tag> listTags(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return tagService.getAll(page, size, sortBy, ascending);
    }

    @QueryMapping
    public Page<Tag> searchTags(@Argument String name,
                                @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return tagService.searchOptional(name, page, size, sortBy, ascending);
    }

    @MutationMapping
    public Tag createTag(@Argument String name) {
        return tagService.create(new CreateTagRequest(name));
    }

    @MutationMapping
    public Tag editTag(@Argument Long id, @Argument String name) {
        return tagService.update(new EditTagRequest(id, name));
    }

    @MutationMapping
    public String deleteTag(@Argument Long id) {
        tagService.delete(id);
        return "Tag with ID " + id + " deleted successfully.";
    }
}
