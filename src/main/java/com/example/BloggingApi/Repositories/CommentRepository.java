package com.example.BloggingApi.Repositories;

import com.example.BloggingApi.Domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository  extends JpaRepository<Comment,Long> {

    Page<Comment> findByContentContainingIgnoreCase(String content, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.author.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Comment> findByAuthorUsernameContainingIgnoreCase(@Param("username") String username, Pageable pageable);

}
