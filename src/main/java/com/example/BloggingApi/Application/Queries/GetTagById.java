package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Tag;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.TagRepository;
import org.springframework.stereotype.Service;

@Service
public class GetTagById {

    private final TagRepository tagRepository;

    public GetTagById(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag handle(Long tagId) throws NullException {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new NullException("Tag not found"));
    }
}
