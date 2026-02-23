package com.example.BloggingApi.Application.Transactions;

import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.DTOs.Requests.CreateCommentRequest;
import com.example.BloggingApi.Repositories.CommentRepository;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that when an unchecked exception occurs during a transactional create operation,
 * the transaction rolls back and the database state is unchanged.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TransactionRollbackTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long savedUserId;
    private Long savedPostId;

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {
            User user = User.create("rollback-user", "rollback@test.com", "password");
            user = userRepository.save(user);
            Post post = Post.create("Rollback test post", "Content", user);
            post = postRepository.save(post);
            savedUserId = user.getId();
            savedPostId = post.getId();
        });
    }

    @Test
    void createComment_WhenExceptionThrownAfterSave_ShouldRollbackAndLeaveDatabaseUnchanged() {
        long countBefore = commentRepository.count();

        CreateCommentRequest request = new CreateCommentRequest(
                "Comment that will be rolled back",
                savedPostId,
                savedUserId
        );

        assertThrows(RuntimeException.class, () ->
                transactionTemplate.executeWithoutResult(status -> {
                    commentService.create(request);
                    throw new RuntimeException("Intentional rollback");
                })
        );

        long countAfter = commentRepository.count();
        assertEquals(countBefore, countAfter,
                "Comment count should be unchanged after rollback; transaction did not roll back correctly.");
    }
}
