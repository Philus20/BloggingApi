package com.example.BloggingApi.API.Controllers.Graphql;

import com.example.BloggingApi.API.Requests.CreateTagRequest;
import com.example.BloggingApi.API.Requests.EditTagRequest;
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
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class TagGraphQLController {

    private final CreateTag createTagHandler;
    private final EditTag editTagHandler;
    private final DeleteTag deleteTagHandler;
    private final GetTagById getTagByIdHandler;
    private final GetAllTags getAllTagsHandler;
    private final SearchTags searchTagsHandler;

    public TagGraphQLController(CreateTag createTagHandler, EditTag editTagHandler, DeleteTag deleteTagHandler, GetTagById getTagByIdHandler, GetAllTags getAllTagsHandler, SearchTags searchTagsHandler) {
        this.createTagHandler = createTagHandler;
        this.editTagHandler = editTagHandler;
        this.deleteTagHandler = deleteTagHandler;
        this.getTagByIdHandler = getTagByIdHandler;
        this.getAllTagsHandler = getAllTagsHandler;
        this.searchTagsHandler = searchTagsHandler;
    }

    @QueryMapping
    public Tag getTag(@Argument Long id) {
        return getTagByIdHandler.handle(id);
    }

    @QueryMapping
    public Page<Tag> listTags(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return getAllTagsHandler.handle(pageable);
    }

    @QueryMapping
    public Page<Tag> searchTags(@Argument String name,
                                @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return searchTagsHandler.searchByName(name, pageable);
    }

    @MutationMapping
    public Tag createTag(@Argument String name) {
        CreateTagRequest request = new CreateTagRequest(name);
        return createTagHandler.handle(request);
    }

    @MutationMapping
    public Tag editTag(@Argument Long id, @Argument String name) {
        EditTagRequest request = new EditTagRequest(id, name);
        return editTagHandler.handle(request);
    }

    @MutationMapping
    public String deleteTag(@Argument Long id) {
        deleteTagHandler.handle(id);
        return "Tag with ID " + id + " deleted successfully.";
    }
}
