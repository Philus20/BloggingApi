package com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories;

import com.example.BloggingApi.Domain.Entities.Review;

import com.example.BloggingApi.Infrastructure.Persistence.Database.DbInterfaces.IConnection;
import com.example.BloggingApi.Infrastructure.Persistence.Database.DbInterfaces.ICrudQueries;
import com.example.BloggingApi.Infrastructure.Persistence.Database.DbInterfaces.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public class ReviewRepository implements Repository<Review> {

    private final IConnection connectionService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ICrudQueries crudQueries;

    @Autowired
    public ReviewRepository(IConnection connectionService, UserRepository userRepository, PostRepository postRepository, ICrudQueries crudQueries) {
        this.connectionService = connectionService;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.crudQueries = crudQueries;
    }


    @Override
    public void create(Review obj) {

    }

    @Override
    public Review findByInteger(int id) {
        return null;
    }

    @Override
    public Review findByString(String str) {
        return null;
    }

    @Override
    public List<Review> findAll() {
        return List.of();
    }

    @Override
    public void updateById(int id) {

    }

    @Override
    public void delete(int id) {

    }

    public void delete(Review review) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.deleteByIdQuery(review.getId().intValue(), "reviews", "review_id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete review", e);
        }
    }

    // Additional methods for query services
    public Page<Review> findAll(Pageable pageable) {
        // Implementation needed
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public Optional<Review> findById(Long id) {
        // Implementation needed
        return Optional.empty();
    }

    public Page<Review> findByCommentContainingIgnoreCase(String comment, Pageable pageable) {
        // Implementation needed
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public Page<Review> findByRating(int rating, Pageable pageable) {
        // Implementation needed
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public Page<Review> findByUserUsernameContainingIgnoreCase(String username, Pageable pageable) {
        // Implementation needed
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }
}
