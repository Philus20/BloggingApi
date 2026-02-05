package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Tag;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchTags {

    private final TagRepository tagRepository;

    public SearchTags(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Search tags by name
     * Uses idx_tag_name index for efficient searching
     */
    public Page<Tag> searchByName(String name, Pageable pageable) {
        return tagRepository.findByNameContainingIgnoreCase(name, pageable);
    }
}
