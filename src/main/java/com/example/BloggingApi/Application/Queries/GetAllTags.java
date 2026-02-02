package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Tag;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GetAllTags {

    private final TagRepository tagRepository;

    public GetAllTags(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Page<Tag> handle(Pageable pageable) {
        return tagRepository.findAll(pageable);
    }
}

