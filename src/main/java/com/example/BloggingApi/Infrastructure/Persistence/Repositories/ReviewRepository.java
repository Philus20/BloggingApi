package com.example.BloggingApi.Infrastructure.Persistence.Repositories;

import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Domain.Repositories.IRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, IRepository<Review> {
}
