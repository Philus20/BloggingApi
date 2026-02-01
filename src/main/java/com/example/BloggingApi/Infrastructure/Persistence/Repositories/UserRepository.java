package com.example.BloggingApi.Infrastructure.Persistence.Repositories;

import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Repositories.IRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository  extends JpaRepository<User, Long>, IRepository<User> {


    User findByUsername(String username);
}
