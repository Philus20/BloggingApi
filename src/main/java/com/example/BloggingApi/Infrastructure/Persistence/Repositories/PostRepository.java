package com.example.BloggingApi.Infrastructure.Persistence.Repositories;

import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Repositories.IRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, IRepository<Post> {

}
