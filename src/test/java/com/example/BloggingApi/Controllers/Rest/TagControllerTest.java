package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.Domain.Tag;
import com.example.BloggingApi.Repositories.TagRepository;
import com.example.BloggingApi.Services.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;
    @MockBean
    private TagRepository tagRepository;

    @Test
    void getTagById_shouldReturn200() throws Exception {
        Tag tag = Tag.create("java");
        when(tagService.getById(1L)).thenReturn(tag);

        mockMvc.perform(get("/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.name").value("java"));
    }

    @Test
    void getAllTags_shouldReturn200() throws Exception {
        Tag tag = Tag.create("spring");
        when(tagService.getAll(0, 5, "id", true))
                .thenReturn(new PageImpl<>(List.of(tag), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("spring"));
    }

    @Test
    void searchTags_shouldReturn200() throws Exception {
        Tag tag = Tag.create("jvm");
        when(tagService.searchByName(eq("jvm"), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(tag), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/tags/search").param("name", "jvm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void createTag_shouldReturn200() throws Exception {
        Tag tag = Tag.create("newtag");
        when(tagService.create(any())).thenReturn(tag);

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"newtag\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tag created successfully"));
    }

    @Test
    void editTag_shouldReturn200() throws Exception {
        Tag tag = Tag.create("updated");
        when(tagService.update(any())).thenReturn(tag);
        when(tagRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(put("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTag_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tag deleted successfully"));
    }
}
