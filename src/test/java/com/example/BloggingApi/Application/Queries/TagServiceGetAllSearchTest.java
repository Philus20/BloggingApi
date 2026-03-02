package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Tag;
import com.example.BloggingApi.Repositories.TagRepository;
import com.example.BloggingApi.Services.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TagServiceGetAllSearchTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll_withPageable_returnsPage() {
        Page<Tag> page = new PageImpl<>(Collections.emptyList());
        when(tagRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Tag> result = tagService.getAll(PageRequest.of(0, 5));

        assertNotNull(result);
        verify(tagRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAll_withPageSize_callsFindAll() {
        Page<Tag> page = new PageImpl<>(Collections.emptyList());
        when(tagRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Tag> result = tagService.getAll(0, 5, "id", true);

        assertNotNull(result);
        verify(tagRepository).findAll(any(Pageable.class));
    }

    @Test
    void searchByName_withPageable_returnsPage() {
        Page<Tag> page = new PageImpl<>(Collections.emptyList());
        when(tagRepository.findByNameContainingIgnoreCase(eq("java"), any())).thenReturn(page);

        Page<Tag> result = tagService.searchByName("java", PageRequest.of(0, 5));

        assertNotNull(result);
        verify(tagRepository).findByNameContainingIgnoreCase(eq("java"), any());
    }

    @Test
    void searchByName_overload_callsSearchByName() {
        Page<Tag> page = new PageImpl<>(Collections.emptyList());
        when(tagRepository.findByNameContainingIgnoreCase(eq("spring"), any())).thenReturn(page);

        Page<Tag> result = tagService.searchByName("spring", 0, 5, "name", true);

        assertNotNull(result);
        verify(tagRepository).findByNameContainingIgnoreCase(eq("spring"), any());
    }

    @Test
    void searchOptional_withName_returnsPage() {
        Page<Tag> page = new PageImpl<>(Collections.emptyList());
        when(tagRepository.findByNameContainingIgnoreCase(eq("tag"), any())).thenReturn(page);

        Page<Tag> result = tagService.searchOptional("tag", 0, 5, "name", true);

        assertNotNull(result);
        verify(tagRepository).findByNameContainingIgnoreCase(eq("tag"), any());
    }

    @Test
    void searchOptional_withBlankName_returnsEmptyPage() {
        Page<Tag> result = tagService.searchOptional(null, 0, 5, "name", true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tagRepository, never()).findByNameContainingIgnoreCase(any(), any());
    }
}
