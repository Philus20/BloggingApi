package com.example.BloggingApi.Infrastructure.Persistence.Repositories;

import com.example.BloggingApi.Domain.Entities.Tag;
import com.example.BloggingApi.Domain.Repositories.IRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, IRepository<Tag> {
    Optional<Tag> findByName(String name);
}
