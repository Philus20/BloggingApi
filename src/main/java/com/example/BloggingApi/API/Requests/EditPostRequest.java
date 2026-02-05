package com.example.BloggingApi.API.Requests;

import com.example.BloggingApi.API.Validation.Numeric;

public record EditPostRequest (@Numeric
                               Long id, String title, String content)

{}