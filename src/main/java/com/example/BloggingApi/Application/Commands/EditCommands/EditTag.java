package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.API.Requests.EditTagRequest;
import com.example.BloggingApi.Domain.Entities.Tag;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EditTag {

    private final TagRepository tagRepository;

    public EditTag(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Tag handle(EditTagRequest request) throws NullException {
        Tag tag = tagRepository.findById(request.id())
                .orElseThrow(() -> new NullException("Tag not found"));

        tag.update(request.name());

        return tag;
    }
}
