package com.example.BloggingApi.Infrastructure.Persistence.Repositories;

import com.example.BloggingApi.Domain.Entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository  extends JpaRepository<Comment,Long> {


}
