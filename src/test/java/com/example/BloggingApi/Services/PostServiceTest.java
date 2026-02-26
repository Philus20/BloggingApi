package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreatePostRequest;
import com.example.BloggingApi.DTOs.Requests.EditPostRequest;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
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
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User author;
    private Post post;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        author = User.create("author", "a@example.com", "pass");
        post = Post.create("Title", "Content", author);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_shouldSaveAndReturnPost() {
        CreatePostRequest req = new CreatePostRequest("T", "C", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        Post result = postService.create(req);
        assertNotNull(result);
        assertEquals("T", result.getTitle());
        assertEquals("C", result.getContent());
        verify(userRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void create_shouldThrowWhenAuthorNotFound() {
        CreatePostRequest req = new CreatePostRequest("T", "C", 999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NullException.class, () -> postService.create(req));
        verify(postRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnPost() {
        EditPostRequest req = new EditPostRequest(1L, "New Title", "New Content");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        Post result = postService.update(req);
        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getContent());
        verify(postRepository).findById(1L);
    }

    @Test
    void update_shouldThrowWhenPostNotFound() {
        EditPostRequest req = new EditPostRequest(999L, "T", "C");
        when(postRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NullException.class, () -> postService.update(req));
    }

    @Test
    void delete_shouldDeletePost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).delete(post);
        postService.delete(1L);
        verify(postRepository).findById(1L);
        verify(postRepository).delete(post);
    }

    @Test
    void delete_shouldThrowWhenPostNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NullException.class, () -> postService.delete(999L));
    }

    @Test
    void getById_shouldReturnPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        Post result = postService.getById(1L);
        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        verify(postRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NullException.class, () -> postService.getById(999L));
    }

    @Test
    void getAll_withPageable_shouldReturnPage() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findAll(pageable)).thenReturn(page);
        Page<Post> result = postService.getAll(pageable);
        assertEquals(1, result.getContent().size());
        verify(postRepository).findAll(pageable);
    }

    @Test
    void getAll_withParams_shouldDelegate() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<Post> result = postService.getAll(0, 5, "id", true);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void search_byKeyword_shouldCallSearchByKeyword() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.searchByKeyword("key", pageable)).thenReturn(page);
        Page<Post> result = postService.search("key", null, null, pageable);
        assertEquals(1, result.getContent().size());
        verify(postRepository).searchByKeyword("key", pageable);
    }

    @Test
    void search_byTitle_shouldCallFindByTitle() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findByTitleContainingIgnoreCase("tit", pageable)).thenReturn(page);
        Page<Post> result = postService.search(null, "tit", null, pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void search_byAuthor_shouldCallFindByAuthor() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findByAuthorUsernameContainingIgnoreCase("auth", pageable)).thenReturn(page);
        Page<Post> result = postService.search(null, null, "auth", pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void search_withNoParams_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> postService.search(null, null, null, pageable));
    }

    @Test
    void searchOptional_withKeyword_shouldReturnPage() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.searchByKeyword(eq("key"), any(Pageable.class))).thenReturn(page);
        Page<Post> result = postService.searchOptional("key", null, null, 0, 5, "id", true);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchOptional_withNoParams_shouldReturnEmptyPage() {
        Page<Post> result = postService.searchOptional(null, null, null, 0, 5, "id", true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchByKeyword_shouldDelegate() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.searchByKeyword("q", pageable)).thenReturn(page);
        Page<Post> result = postService.searchByKeyword("q", pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchByTitle_shouldDelegate() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findByTitleContainingIgnoreCase("t", pageable)).thenReturn(page);
        Page<Post> result = postService.searchByTitle("t", pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchByAuthor_shouldDelegate() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findByAuthorUsernameContainingIgnoreCase("a", pageable)).thenReturn(page);
        Page<Post> result = postService.searchByAuthor("a", pageable);
        assertEquals(1, result.getContent().size());
    }
}
