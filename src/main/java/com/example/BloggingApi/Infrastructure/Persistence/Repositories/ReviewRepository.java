package com.example.BloggingApi.Infrastructure.Persistence.Repositories;

import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Domain.Repositories.IRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, IRepository<Review> {
    
    // Search by comment content
    Page<Review> findByCommentContainingIgnoreCase(String comment, Pageable pageable);

    // Filter by rating
    Page<Review> findByRating(int rating, Pageable pageable);

    // Search by author (User) username
    @Query("SELECT r FROM Review r WHERE LOWER(r.user.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Review> findByUserUsernameContainingIgnoreCase(@Param("username") String username, Pageable pageable);
}

