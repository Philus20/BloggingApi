package com.example.BloggingApi.Infrastructure.Persistence.Repositories;

import com.example.BloggingApi.Domain.Entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository  extends JpaRepository<Comment,Long> {

    // Search by content
    Page<Comment> findByContentContainingIgnoreCase(String content, Pageable pageable);

    // Search by author username
    @Query("SELECT c FROM Comment c WHERE LOWER(c.author.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Comment> findByAuthorUsernameContainingIgnoreCase(@Param("username") String username, Pageable pageable);

}

