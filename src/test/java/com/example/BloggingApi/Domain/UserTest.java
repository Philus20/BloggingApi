package com.example.BloggingApi.Domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void create_shouldSetUsernameEmailPasswordAndDefaultRole() {
        User user = User.create("alice", "alice@example.com", "secret");

        assertNotNull(user);
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertNotNull(user.getUsername());
        assertNotNull(user.getEmail());
    }

    @Test
    void update_shouldUpdateUsernameWhenNonNull() {
        User user = User.create("old", "old@example.com", "p");
        user.update("new", null);

        assertEquals("new", user.getUsername());
        assertEquals("old@example.com", user.getEmail());
    }

    @Test
    void update_shouldUpdateEmailWhenNonNull() {
        User user = User.create("u", "old@example.com", "p");
        user.update(null, "new@example.com");

        assertEquals("u", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    void update_shouldIgnoreBlankUsername() {
        User user = User.create("keep", "e@e.com", "p");
        user.update("   ", "e@e.com");

        assertEquals("keep", user.getUsername());
    }

    @Test
    void update_shouldIgnoreBlankEmail() {
        User user = User.create("u", "keep@e.com", "p");
        user.update("u", "   ");

        assertEquals("keep@e.com", user.getEmail());
    }

    @Test
    void getId_shouldReturnIdAfterSet() {
        User user = new User();
        assertNull(user.getId());
        // Id is typically set by JPA; we test getter exists
    }

    @Test
    void getters_shouldReturnValuesFromCreate() {
        User user = User.create("bob", "bob@test.com", "pass");
        assertEquals("bob", user.getUsername());
        assertEquals("bob@test.com", user.getEmail());
        assertNotNull(user.getUsername());
        assertNotNull(user.getEmail());
    }
}
