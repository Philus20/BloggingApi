package com.example.BloggingApi.Infrastructure.Persistence.Repositories;

import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Repositories.IRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository  extends JpaRepository<User, Long>, IRepository<User> {

    User findByUsername(String username);

    // Search by username (uses idx_user_username index)
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    // Search by email (uses idx_user_email index)
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    // Combined search in username or email
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

