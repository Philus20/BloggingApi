package com.example.BloggingApi.Services;

import com.example.BloggingApi.RequestsDTO.CreateTagRequest;
import com.example.BloggingApi.Entities.Tag;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.TagRepository;
import com.example.BloggingApi.RequestsDTO.EditTagRequest;
import com.example.BloggingApi.ResposesDTO.TagResponse;
import com.example.BloggingApi.Validation.ValidateSearchParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final ValidateSearchParams validateSearchParams;

    public TagService(TagRepository tagRepository, ValidateSearchParams validateSearchParams) {
        this.tagRepository = tagRepository;
        this.validateSearchParams = validateSearchParams;
    }

    public Tag createTag(CreateTagRequest req)  {





        // Create Tag entity
        Tag tag = Tag.create(req.name());

        // Save to DB
        tagRepository.create(tag);

        return tag;
    }

    public Tag editTag(EditTagRequest request) throws NullException {
        Tag tag = tagRepository.findByInteger(request.id().intValue());

        if (tag == null) {
            throw new NullException("Tag not found");
        }

        tag.update(request.name());

        return tag;
    }

    public void deleteTag(Long tagId) throws NullException {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NullException("Tag not found"));

        tagRepository.delete(tag);
    }


    //Query to get all tags
    public Page<TagResponse> getAllTags(
            int page,
            int size,
            String sortBy,
            boolean ascending
    ) {
        Sort sort = Sort.by(
                ascending ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );

        Pageable pageable = PageRequest.of(page, size, sort);

        return tagRepository.findAll(pageable)
                .map(TagResponse::from);
    }


    public Tag getTagById(Long tagId) throws NullException {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new NullException("Tag not found"));
    }

    public Page<TagResponse> searchTags(
            String name,
            int page,
            int size,
            String sortBy,
            boolean ascending
    ) {
        validateSearchParams.hasText(name);

        Sort sort = Sort.by(
                ascending ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        return tagRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(TagResponse::from);
    }



}
