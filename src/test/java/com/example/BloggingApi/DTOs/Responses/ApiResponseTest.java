package com.example.BloggingApi.DTOs.Responses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_withData_shouldReturnCorrectResponse() {
        ApiResponse<String> response = ApiResponse.success("Done", "data");

        assertTrue(response.status());
        assertEquals("Done", response.message());
        assertEquals("data", response.data());
    }

    @Test
    void success_withoutData_shouldReturnNullData() {
        ApiResponse<Void> response = ApiResponse.success("Deleted");

        assertTrue(response.status());
        assertEquals("Deleted", response.message());
        assertNull(response.data());
    }

    @Test
    void failure_shouldReturnCorrectResponse() {
        ApiResponse<Void> response = ApiResponse.failure("Error message");

        assertFalse(response.status());
        assertEquals("Error message", response.message());
        assertNull(response.data());
    }
}
