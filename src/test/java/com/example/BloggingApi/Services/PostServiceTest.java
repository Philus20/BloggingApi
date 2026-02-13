package com.example.BloggingApi.Services;

import com.example.BloggingApi.Entities.Post;
import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.RequestsDTO.CreatePostRequest;
import com.example.BloggingApi.RequestsDTO.EditPostRequest;
import com.example.BloggingApi.ResposesDTO.PostsResponse;
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
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User author;
    private Post post;

    @BeforeEach
    void setUp() {
        author = User.create("john", "john@example.com", "password");
        post = Post.create("Title", "Content", author);
    }

    @Test
    void getPostById_returnsMappedResponse_whenPostExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostsResponse result = postService.getPostById(1L);

        assertThat(result.id()).isEqualTo(post.getId());
        assertThat(result.title()).isEqualTo("Title");
        verify(postRepository).findById(1L);
    }

    @Test
    void getPostById_throwsNullException_whenPostMissing() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(1L))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void getAllPosts_returnsPagedResponses() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Post> page = new PageImpl<>(List.of(post), pageable, 1);
        when(postRepository.findAll(any())).thenReturn(page);

        Page<PostsResponse> result = postService.getAllPosts(0, 5, "createdAt", false);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("Title");
        verify(postRepository).findAll(any());
    }



    @Test
    void searchPosts_usesKeywordSearch_whenKeywordPresent() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Post> page = new PageImpl<>(List.of(post), pageable, 1);
        when(postRepository.searchByKeyword(eq("spring"), any())).thenReturn(page);

        Page<PostsResponse> result = postService.searchPosts("spring", null, null, 0, 5, "createdAt", false);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(postRepository).searchByKeyword(eq("spring"), any());
        verify(postRepository, never()).findByTitleContainingIgnoreCase(anyString(), any());
        verify(postRepository, never()).findByAuthorUsernameContainingIgnoreCase(anyString(), any());
    }

    @Test
    void searchPosts_throwsIllegalArgumentException_whenNoSearchParam() {
        assertThatThrownBy(() -> postService.searchPosts(null, null, null, 0, 5, "createdAt", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provide at least one search parameter");
    }



    @Test
    void createPost_createsAndReturnsResponse_whenAuthorExists() {
        CreatePostRequest request = new CreatePostRequest("New Title", "New Content", 10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(author));

        PostsResponse result = postService.createPost(request);

        assertThat(result.title()).isEqualTo("New Title");
        verify(userRepository).findById(10L);
        verify(postRepository).create(any(Post.class));
    }



    @Test
    void createPost_throwsNullException_whenAuthorMissing() {
        CreatePostRequest request = new CreatePostRequest("New Title", "New Content", 10L);
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Author not found");
    }



    @Test
    void editPost_updatesPost_whenItExists() {
        EditPostRequest request = new EditPostRequest(1L, "Updated", "Updated content");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostsResponse result = postService.editPost(request);

        assertThat(result.title()).isEqualTo("Updated");
        verify(postRepository).findById(1L);
    }

    @Test
    void editPost_throwsNullException_whenPostMissing() {
        EditPostRequest request = new EditPostRequest(1L, "Updated", "Updated content");
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.editPost(request))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void deletePost_deletes_whenPostExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L);

        verify(postRepository).findById(1L);
        verify(postRepository).delete(1);
    }

    @Test
    void deletePost_throwsNullException_whenPostMissing() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(1L))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Post not found");
    }
}
