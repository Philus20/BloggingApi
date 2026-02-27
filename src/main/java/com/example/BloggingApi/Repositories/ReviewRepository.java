package com.example.BloggingApi.Repositories;

import com.example.BloggingApi.Domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "reviews", path = "reviews")
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user", "post"})
    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findByIdWithRelations(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"user", "post"})
    Page<Review> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "post"})
    Page<Review> findByCommentContainingIgnoreCase(String comment, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "post"})
    Page<Review> findByRating(int rating, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "post"})
    Page<Review> findByUserUsernameContainingIgnoreCase(String username, Pageable pageable);
}
