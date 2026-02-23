package com.example.BloggingApi.Utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableUtils {

    private PageableUtils() {}

    public static Pageable create(int page, int size, String sortBy, boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    public static Pageable of(int page, int size, String sortBy, boolean asc) {
        return create(page, size, sortBy, asc);
    }
}
