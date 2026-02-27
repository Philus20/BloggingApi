package com.example.BloggingApi.Repositories;

import com.example.BloggingApi.Domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "tags", path = "tags")
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
