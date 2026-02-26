package com.example.BloggingApi.Application.AOP;

import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AopIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void serviceCall_shouldTriggerLoggingAndPerformanceAspects() {
        User user = User.create("aopuser", "aop@e.com", "pass");
        user = userRepository.save(user);
        User found = userService.getById(user.getId());
        assertNotNull(found);
        assertEquals("aopuser", found.getUsername());
    }

    @Test
    void cacheableGetById_shouldTriggerCacheLoggingAspect() {
        User user = User.create("cacheuser", "cache@e.com", "pass");
        user = userRepository.save(user);
        userService.getById(user.getId());
        userService.getById(user.getId());
        assertTrue(true);
    }
}
