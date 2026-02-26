package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreateTagRequest;
import com.example.BloggingApi.DTOs.Requests.EditTagRequest;
import com.example.BloggingApi.Domain.Tag;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag tag;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        tag = Tag.create("java");
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_shouldSaveAndReturnTag() {
        CreateTagRequest req = new CreateTagRequest("spring");
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Tag result = tagService.create(req);

        assertNotNull(result);
        assertEquals("spring", result.getName());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void update_shouldUpdateAndReturnTag() {
        EditTagRequest req = new EditTagRequest(1L, "updated");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Tag result = tagService.update(req);

        assertNotNull(result);
        assertEquals("updated", result.getName());
        verify(tagRepository).findById(1L);
    }

    @Test
    void update_shouldThrowWhenTagNotFound() {
        EditTagRequest req = new EditTagRequest(999L, "x");
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> tagService.update(req));
    }

    @Test
    void delete_shouldDeleteTag() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        doNothing().when(tagRepository).delete(tag);

        tagService.delete(1L);

        verify(tagRepository).findById(1L);
        verify(tagRepository).delete(tag);
    }

    @Test
    void delete_shouldThrowWhenTagNotFound() {
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> tagService.delete(999L));
    }

    @Test
    void getById_shouldReturnTag() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Tag result = tagService.getById(1L);

        assertNotNull(result);
        assertEquals("java", result.getName());
        verify(tagRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> tagService.getById(999L));
    }

    @Test
    void getAll_withPageable_shouldReturnPage() {
        Page<Tag> page = new PageImpl<>(List.of(tag));
        when(tagRepository.findAll(pageable)).thenReturn(page);

        Page<Tag> result = tagService.getAll(pageable);

        assertEquals(1, result.getContent().size());
        verify(tagRepository).findAll(pageable);
    }

    @Test
    void getAll_withParams_shouldDelegate() {
        Page<Tag> page = new PageImpl<>(List.of(tag));
        when(tagRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Tag> result = tagService.getAll(0, 5, "id", true);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchByName_shouldDelegate() {
        Page<Tag> page = new PageImpl<>(List.of(tag));
        when(tagRepository.findByNameContainingIgnoreCase("jav", pageable)).thenReturn(page);

        Page<Tag> result = tagService.searchByName("jav", pageable);

        assertEquals(1, result.getContent().size());
        verify(tagRepository).findByNameContainingIgnoreCase("jav", pageable);
    }

    @Test
    void searchByName_withPageParams_shouldDelegate() {
        Page<Tag> page = new PageImpl<>(List.of(tag));
        when(tagRepository.findByNameContainingIgnoreCase(eq("x"), any(Pageable.class))).thenReturn(page);

        Page<Tag> result = tagService.searchByName("x", 0, 10, "name", true);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchOptional_withName_shouldReturnPage() {
        Page<Tag> page = new PageImpl<>(List.of(tag));
        when(tagRepository.findByNameContainingIgnoreCase(eq("tag"), any(Pageable.class))).thenReturn(page);

        Page<Tag> result = tagService.searchOptional("tag", 0, 5, "id", true);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchOptional_withNoName_shouldReturnEmptyPage() {
        Page<Tag> result = tagService.searchOptional(null, 0, 5, "id", true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
