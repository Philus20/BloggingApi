package com.example.BloggingApi.Utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class PageableUtilsTest {

    @Test
    void create_ShouldProducePageableWithCorrectPageAndSize() {
        Pageable pageable = PageableUtils.create(2, 10, "id", true);

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void create_ShouldApplyAscendingSort() {
        Pageable pageable = PageableUtils.create(0, 5, "createdAt", true);

        Sort sort = pageable.getSort();
        assertNotNull(sort);
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("createdAt").getDirection());
    }

    @Test
    void create_ShouldApplyDescendingSort() {
        Pageable pageable = PageableUtils.create(0, 5, "title", false);

        Sort sort = pageable.getSort();
        assertNotNull(sort);
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("title").getDirection());
    }

    @Test
    void of_ShouldDelegateToCreate() {
        Pageable pageable = PageableUtils.of(1, 20, "name", false);

        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("name").getDirection());
    }
}
