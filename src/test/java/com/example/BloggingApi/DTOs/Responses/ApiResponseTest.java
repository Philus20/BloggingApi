package com.example.BloggingApi.DTOs.Responses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_withMessageAndData_shouldReturnTrueStatus() {
        ApiResponse<String> res = ApiResponse.success("Created", "data");
        assertTrue(res.status());
        assertEquals("Created", res.message());
        assertEquals("data", res.data());
    }

    @Test
    void success_withMessageOnly_shouldHaveNullData() {
        ApiResponse<Void> res = ApiResponse.success("OK");
        assertTrue(res.status());
        assertEquals("OK", res.message());
        assertNull(res.data());
    }

    @Test
    void failure_shouldReturnFalseStatusAndMessage() {
        ApiResponse<Void> res = ApiResponse.failure("Error message");
        assertFalse(res.status());
        assertEquals("Error message", res.message());
        assertNull(res.data());
    }
}
