package com.example.BloggingApi.Services;

import com.example.BloggingApi.Entities.Tag;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.TagRepository;
import com.example.BloggingApi.RequestsDTO.CreateTagRequest;
import com.example.BloggingApi.RequestsDTO.EditTagRequest;
import com.example.BloggingApi.ResposesDTO.TagResponse;
import com.example.BloggingApi.Validation.ValidateSearchParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ValidateSearchParams validateSearchParams;

    @InjectMocks
    private TagService tagService;

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = Tag.create("tech");
    }

    @Test
    void createTag_createsAndReturnsTag() {
        CreateTagRequest request = new CreateTagRequest("tech");

        Tag result = tagService.createTag(request);

        assertThat(result.getName()).isEqualTo("tech");
        verify(tagRepository).create(any(Tag.class));
    }

    @Test
    void editTag_updatesExistingTag_whenFound() {
        EditTagRequest request = new EditTagRequest(1L, "updated");
        when(tagRepository.findByInteger(1)).thenReturn(tag);

        Tag result = tagService.editTag(request);

        assertThat(result.getName()).isEqualTo("updated");
        verify(tagRepository).findByInteger(1);
    }

    @Test
    void editTag_throwsNullException_whenNotFound() {
        EditTagRequest request = new EditTagRequest(1L, "updated");
        when(tagRepository.findByInteger(1)).thenReturn(null);

        assertThatThrownBy(() -> tagService.editTag(request))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Tag not found");
    }

    @Test
    void deleteTag_deletes_whenFound() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        tagService.deleteTag(1L);

        verify(tagRepository).findById(1L);
        verify(tagRepository).delete(tag);
    }

    @Test
    void deleteTag_throwsNullException_whenNotFound() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.deleteTag(1L))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Tag not found");
    }

    @Test
    void getAllTags_returnsPageOfResponses() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Tag> page = new PageImpl<>(List.of(tag), pageable, 1);
        when(tagRepository.findAll(any())).thenReturn(page);

        Page<TagResponse> result = tagService.getAllTags(0, 5, "id", true);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(tagRepository).findAll(any());
    }

    @Test
    void getTagById_returnsTag_whenFound() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Tag result = tagService.getTagById(1L);

        assertThat(result).isEqualTo(tag);
        verify(tagRepository).findById(1L);
    }

    @Test
    void getTagById_throwsNullException_whenNotFound() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getTagById(1L))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Tag not found");
    }

    @Test
    void searchTags_returnsPage_whenNameProvided() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Tag> page = new PageImpl<>(List.of(tag), pageable, 1);
        when(tagRepository.findByNameContainingIgnoreCase(eq("tech"), any()))
                .thenReturn(page);

        Page<TagResponse> result = tagService.searchTags("tech", 0, 5, "name", true);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(validateSearchParams).hasText("tech");
        verify(tagRepository).findByNameContainingIgnoreCase(eq("tech"), any());
    }
}
