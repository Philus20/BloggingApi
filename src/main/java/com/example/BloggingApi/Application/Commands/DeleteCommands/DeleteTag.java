package com.example.BloggingApi.Application.Commands.DeleteCommands;

import com.example.BloggingApi.Domain.Entities.Tag;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeleteTag {

    private final TagRepository tagRepository;

    public DeleteTag(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public void handle(Long tagId) throws NullException {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NullException("Tag not found"));

        tagRepository.delete(tag);
    }
}
