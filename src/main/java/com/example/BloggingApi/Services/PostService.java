package com.example.BloggingApi.Services;

import com.example.BloggingApi.RequestsDTO.CreatePostRequest;
import com.example.BloggingApi.Entities.Post;
import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.RequestsDTO.EditPostRequest;
import com.example.BloggingApi.ResposesDTO.PostsResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "posts", key = "#id")
    public PostsResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NullException("Post not found"));
        return PostsResponse.from(post);
    }

    @Cacheable(value = "postPages", key = "#page + '-' + #size + '-' + #sortBy + '-' + #ascending")
    public Page<PostsResponse> getAllPosts(
            int page, int size, String sortBy, boolean ascending
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, ascending);
        return postRepository.findAll(pageable)
                .map(PostsResponse::from);
    }

    public Page<PostsResponse> searchPosts(
            String keyword, String title, String author,
            int page, int size, String sortBy, boolean ascending
    ) {
        validateSearch(keyword, title, author);

        Pageable pageable = buildPageable(page, size, sortBy, ascending);
        Page<Post> posts;

        if (hasText(keyword)) {
            posts = postRepository.searchByKeyword(keyword, pageable);
        } else if (hasText(title)) {
            posts = postRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            posts = postRepository.findByAuthorUsernameContainingIgnoreCase(author, pageable);
        }

        return posts.map(PostsResponse::from);
    }

    @CacheEvict(value = {"posts", "postPages"}, allEntries = true)
    public PostsResponse createPost(CreatePostRequest req) {
        User author = userRepository.findById(req.authorId())
                .orElseThrow(() -> new NullException("Author not found"));

        Post post = Post.create(req.title(), req.content(), author);
        postRepository.create(post);

        return PostsResponse.from(post);
    }

    public PostsResponse editPost(EditPostRequest req) {
        Post post = postRepository.findById(req.id())
                .orElseThrow(() -> new NullException("Post not found"));

        post.update(req.title(), req.content());
        return PostsResponse.from(post);
    }


    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NullException("Post not found"));

        postRepository.delete(id.intValue());
    }


    // ---- helpers ----

    private Pageable buildPageable(int page, int size, String sortBy, boolean asc) {
        Sort sort = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        return PageRequest.of(page, size, sort);
    }

    private void validateSearch(String keyword, String title, String author) {
        if (!hasText(keyword) && !hasText(title) && !hasText(author)) {
            throw new IllegalArgumentException(
                    "Provide at least one search parameter: keyword, title, or author"
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
