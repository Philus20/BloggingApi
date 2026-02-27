package com.example.BloggingApi.Repositories;

import com.example.BloggingApi.Domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "comments", path = "comments")
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author", "post"})
    @Query("SELECT c FROM Comment c WHERE c.id = :id")
    Optional<Comment> findByIdWithRelations(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"author", "post"})
    Page<Comment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "post"})
    Page<Comment> findByContentContainingIgnoreCase(String content, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "post"})
    Page<Comment> findByAuthorUsernameContainingIgnoreCase(String username, Pageable pageable);
}
