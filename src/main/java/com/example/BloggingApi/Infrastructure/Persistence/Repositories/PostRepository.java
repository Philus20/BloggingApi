package com.example.BloggingApi.Infrastructure.Persistence.Repositories;

import com.example.BloggingApi.Domain.Entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Search by title (uses idx_post_title index)
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Search by content
    Page<Post> findByContentContainingIgnoreCase(String content, Pageable pageable);

    // Combined search in title or content
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Find posts by author username
    @Query("SELECT p FROM Post p WHERE LOWER(p.author.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Post> findByAuthorUsernameContainingIgnoreCase(@Param("username") String username, Pageable pageable);

}
