package com.example.BloggingApi.Services;

import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Repositories.CachingRepositoryTest;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class CachingServiceForTest {

    private final CachingRepositoryTest cachingRepositoryTest;

    public CachingServiceForTest(CachingRepositoryTest cachingRepositoryTest) {
        this.cachingRepositoryTest = cachingRepositoryTest;
    }

    /**
     * Get user by ID and cache result
     */
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        System.out.println("Fetching from DB: user " + id);
        return cachingRepositoryTest.findByInteger(id.intValue());
    }

    /**
     * Get all users and cache result as a single key
     */
    @Cacheable(value = "users", key = "'all'")
    public List<User> getAllUsers() {
        System.out.println("Fetching all users from DB");
        return cachingRepositoryTest.findAll();
    }

    /**
     * Update user and refresh cache for both individual and all-users cache
     */
    @CachePut(value = "users", key = "#user.id")
    public User editUser(User user) {
        cachingRepositoryTest.update(user);
        return user;
    }

    /**
     * Delete user and evict from cache
     */
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(int userId) {
        cachingRepositoryTest.delete(userId);
    }

    /**
     * Evict all users cache manually if needed
     */
    @CacheEvict(value = "users", key = "'all'")
    public void evictAllUsersCache() {
        // This will remove the cached list of all users
    }
}
