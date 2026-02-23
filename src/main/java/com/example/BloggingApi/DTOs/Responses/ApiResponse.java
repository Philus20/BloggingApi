package com.example.BloggingApi.DTOs.Responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API response wrapper")
public record ApiResponse<T>(
        @Schema(description = "Whether the operation succeeded") boolean status,
        @Schema(description = "Human-readable message") String message,
        @Schema(description = "Response payload (null for delete or on error)") T data
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static  <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }

}
