package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.Domain.Tag;
import com.example.BloggingApi.DTOs.Requests.CreateTagRequest;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.TagRepository;
import com.example.BloggingApi.Services.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TagService tagService;

    @MockBean
    private TagRepository tagRepository;

    @Test
    @WithMockUser(roles = "READER")
    void getTagById_shouldReturnTag_whenExists() throws Exception {
        Tag tag = Tag.create("java");
        when(tagService.getById(1L)).thenReturn(tag);

        mockMvc.perform(get("/api/v1/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.name").value("java"));

        verify(tagService).getById(1L);
    }

    @Test
    @WithMockUser(roles = "READER")
    void getTagById_shouldReturn404_whenNotFound() throws Exception {
        when(tagService.getById(999L)).thenThrow(new NullException("Tag not found"));

        mockMvc.perform(get("/api/v1/tags/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void createTag_shouldReturn200_whenValid() throws Exception {
        when(tagRepository.findByName("java")).thenReturn(Optional.empty());
        CreateTagRequest request = new CreateTagRequest("java");
        Tag created = Tag.create("java");
        when(tagService.create(any(CreateTagRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/tags")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(tagService).create(any(CreateTagRequest.class));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void createTag_shouldReturn400_whenValidationFails() throws Exception {
        CreateTagRequest request = new CreateTagRequest("");

        mockMvc.perform(post("/api/v1/tags")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(tagService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void deleteTag_shouldReturn200_whenExists() throws Exception {
        doNothing().when(tagService).delete(1L);

        mockMvc.perform(delete("/api/v1/tags/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(tagService).delete(1L);
    }
}
