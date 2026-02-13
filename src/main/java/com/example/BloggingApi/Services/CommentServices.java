package com.example.BloggingApi.Services;

import com.example.BloggingApi.RequestsDTO.CreateCommentRequest;
import com.example.BloggingApi.Entities.Comment;
import com.example.BloggingApi.Entities.Post;
import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.CommentRepository;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.RequestsDTO.EditCommentRequest;
import com.example.BloggingApi.ResposesDTO.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
@Service
public class CommentServices {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentServices(CommentRepository commentRepository,
                           PostRepository postRepository,
                           UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NullException("Comment not found"));
        return CommentResponse.from(comment);
    }

    public Page<CommentResponse> getAllComments(int page, int size, String sortBy, boolean ascending) {
        Pageable pageable = buildPageable(page, size, sortBy, ascending);
        return commentRepository.findAll(pageable)
                .map(CommentResponse::from);
    }

    public Page<CommentResponse> searchComments(String content, String author,
                                                int page, int size, String sortBy, boolean ascending) {
        if ((content == null || content.isBlank()) && (author == null || author.isBlank())) {
            throw new IllegalArgumentException("Provide at least content or author");
        }

        Pageable pageable = buildPageable(page, size, sortBy, ascending);
        Page<Comment> comments;

        if (content != null && !content.isBlank()) {
            comments = commentRepository.findByContentContainingIgnoreCase(content, pageable);
        } else {
            comments = commentRepository.findByAuthorUsernameContainingIgnoreCase(author, pageable);
        }

        return comments.map(CommentResponse::from);
    }

    public CommentResponse createComment(CreateCommentRequest req) {
        Post post = postRepository.findById(req.postId())
                .orElseThrow(() -> new NullException("Post not found"));

        User author = userRepository.findById(req.authorId())
                .orElseThrow(() -> new NullException("Author not found"));

        Comment comment = Comment.create(req.content(), post, author);
        commentRepository.create(comment);

        return CommentResponse.from(comment);
    }

    public CommentResponse editComment(EditCommentRequest req) {
        Comment comment = commentRepository.findById(req.id())
                .orElseThrow(() -> new NullException("Comment not found"));

        comment.update(req.content());
        return CommentResponse.from(comment);
    }

    public void deleteComment(Long commentId) {
      Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NullException("Comment not found"));
        commentRepository.delete(commentId.intValue());
    }

    // Helper
    private Pageable buildPageable(int page, int size, String sortBy, boolean asc) {
        Sort sort = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        return PageRequest.of(page, size, sort);
    }
}
