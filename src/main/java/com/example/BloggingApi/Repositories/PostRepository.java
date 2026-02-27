package com.example.BloggingApi.Repositories;

import com.example.BloggingApi.Domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "posts", path = "posts")
public interface PostRepository extends JpaRepository<Post, Long> {



    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"author"})
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);


    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Page<Post> findByAuthorUsernameContainingIgnoreCase(String username, Pageable pageable);
}
