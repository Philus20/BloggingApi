package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreateTagRequest;
import com.example.BloggingApi.Domain.Entities.Tag;
import com.example.BloggingApi.Domain.Exceptions.DuplicateEntityException;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.TagRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateTag {

    private final TagRepository tagRepository;

    public CreateTag(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag handle(CreateTagRequest req) throws NullException, DuplicateEntityException {
        // Validation
        if (req.name() == null || req.name().isBlank()) {
            throw new NullException("Tag name cannot be blank");
        }

        // Check for duplicate
        if (tagRepository.findByName(req.name()).isPresent()) {
            throw new DuplicateEntityException("Tag with name '" + req.name() + "' already exists");
        }

        // Create Tag entity
        Tag tag = Tag.create(req.name());

        // Save to DB
        tagRepository.save(tag);

        return tag;
    }
}
