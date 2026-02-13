package com.example.BloggingApi.RequestsDTO;

import com.example.BloggingApi.Validation.Numeric;

public record EditPostRequest (@Numeric
                               Long id, String title, String content)

{}