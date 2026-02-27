package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreateTagRequest;
import com.example.BloggingApi.DTOs.Requests.EditTagRequest;
import com.example.BloggingApi.Domain.Tag;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.TagRepository;
import com.example.BloggingApi.Utils.PageableUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public Tag create(CreateTagRequest req) {
        Tag tag = Tag.create(req.name());
        return tagRepository.save(tag);
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public Tag update(EditTagRequest request) {
        Tag tag = tagRepository.findById(request.id())
                .orElseThrow(() -> new NullException("Tag not found"));
        tag.update(request.name());
        return tag;
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NullException("Tag not found"));
        tagRepository.delete(tag);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "#id")
    public Tag getById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NullException("Tag not found"));
    }

    @Transactional(readOnly = true)
    public Page<Tag> getAll(Pageable pageable) {
        return tagRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Tag> searchByName(String name, Pageable pageable) {
        return tagRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tags")
    public Page<Tag> getAll(int page, int size, String sortBy, boolean ascending) {
        return getAll(PageableUtils.create(page, size, sortBy, ascending));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tags")
    public Page<Tag> searchByName(String name, int page, int size, String sortBy, boolean ascending) {
        return searchByName(name, PageableUtils.create(page, size, sortBy, ascending));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tags")
    public Page<Tag> searchOptional(String name, int page, int size, String sortBy, boolean ascending) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, ascending);
        if (name != null && !name.isBlank()) {
            return tagRepository.findByNameContainingIgnoreCase(name, pageable);
        }
        return Page.empty(pageable);
    }
}
