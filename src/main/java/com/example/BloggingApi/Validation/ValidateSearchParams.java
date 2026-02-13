package com.example.BloggingApi.Validation;

import jakarta.validation.constraints.Email;
import org.springframework.stereotype.Service;

@Service
public class ValidateSearchParams {
    public void Validate(String keyword, String username, @Email String email) {
        if (!hasText(keyword) && !hasText(username) && !hasText(email)) {
            throw new IllegalArgumentException(
                    "Please provide at least one search parameter: keyword, username, or email"
            );
        }
    }

    public void validateSearchParamsForReview(String comment, Integer rating, String author) {
        if (!hasText(comment) && rating == null && !hasText(author)) {
            throw new IllegalArgumentException(
                    "Please provide at least one search parameter: comment, rating, or author"
            );
        }
    }




    public boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}


