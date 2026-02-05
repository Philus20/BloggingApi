package com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories;

import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Infrastructure.Persistence.Database.DbInterfaces.IConnection;
import com.example.BloggingApi.Infrastructure.Persistence.Database.DbInterfaces.ICrudQueries;
import com.example.BloggingApi.Infrastructure.Persistence.Database.DbInterfaces.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public class PostRepository implements Repository<Post> {

    private final IConnection connectionService;
    private final UserRepository userRepository;
    private final ICrudQueries crudQueries;


    @Autowired
    public PostRepository(IConnection connectionService, UserRepository userRepository, ICrudQueries crudQueries) {
        this.connectionService = connectionService;
        this.userRepository = userRepository;
        this.crudQueries = crudQueries;
    }


    @Override
    public void create(Post obj) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.createQuery("posts", "title, content, user_id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, obj.getTitle());
                stmt.setString(2, obj.getContent());
                stmt.setLong(3, obj.getAuthor().getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create post", e);
        }
    }

    @Override
    public Post findByInteger(int id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.getByIntegerQuery(id, "posts", "post_id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToPost(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find post by id", e);
        }
        return null;
    }

    @Override
    public Post findByString(String str) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.getStringQuery(str, "posts", "title");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToPost(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find post by string", e);
        }
        return null;
    }

    @Override
    public List<Post> findAll() {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.getAllQuery("posts");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Post> posts = new ArrayList<>();
                    while (rs.next()) {
                        posts.add(mapResultSetToPost(rs));
                    }
                    return posts;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all posts", e);
        }
    }

    @Override
    public void updateById(int id) {
        // This method is kept for interface compatibility
        // Use update(Post obj) for actual updates
        throw new UnsupportedOperationException("Use update(Post obj) method instead");
    }
    
    public void update(Post obj) {
        try (Connection connection = connectionService.createConnection()) {
            String query = "UPDATE posts SET title = ?, content = ? WHERE post_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, obj.getTitle());
                stmt.setString(2, obj.getContent());
                stmt.setLong(3, obj.getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update post", e);
        }
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("post_id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            post.setAuthor(userRepository.findByInteger(userId));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            post.setCreatedAt(createdAt.toLocalDateTime());
        }

        return post;
    }


    @Override
    public void delete(int id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.deleteByIdQuery(id, "posts", "id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete post", e);
        }
    }

    // Additional methods for query services
    public Page<Post> findAll(Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count
            String countQuery = "SELECT COUNT(*) FROM posts";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results
                    String query = "SELECT * FROM posts ORDER BY created_at DESC LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setInt(1, pageable.getPageSize());
                        stmt.setInt(2, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<Post> posts = new ArrayList<>();
                            while (rs.next()) {
                                posts.add(mapResultSetToPost(rs));
                            }
                            return new PageImpl<>(posts, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all posts with pagination", e);
        }
    }

    public Optional<Post> findById(Long id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = "SELECT * FROM posts WHERE post_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPost(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find post by id", e);
        }
        return Optional.empty();
    }

    public Page<Post> searchByKeyword(String keyword, Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count
            String countQuery = "SELECT COUNT(*) FROM posts WHERE title LIKE ? OR content LIKE ?";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                countStmt.setString(1, searchPattern);
                countStmt.setString(2, searchPattern);
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results
                    String query = "SELECT * FROM posts WHERE title LIKE ? OR content LIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setString(1, searchPattern);
                        stmt.setString(2, searchPattern);
                        stmt.setInt(3, pageable.getPageSize());
                        stmt.setInt(4, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<Post> posts = new ArrayList<>();
                            while (rs.next()) {
                                posts.add(mapResultSetToPost(rs));
                            }
                            return new PageImpl<>(posts, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search posts by keyword", e);
        }
    }

    public Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count
            String countQuery = "SELECT COUNT(*) FROM posts WHERE LOWER(title) LIKE LOWER(?)";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                String searchPattern = "%" + title + "%";
                countStmt.setString(1, searchPattern);
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results
                    String query = "SELECT * FROM posts WHERE LOWER(title) LIKE LOWER(?) ORDER BY created_at DESC LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setString(1, searchPattern);
                        stmt.setInt(2, pageable.getPageSize());
                        stmt.setInt(3, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<Post> posts = new ArrayList<>();
                            while (rs.next()) {
                                posts.add(mapResultSetToPost(rs));
                            }
                            return new PageImpl<>(posts, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find posts by title", e);
        }
    }

    public Page<Post> findByAuthorUsernameContainingIgnoreCase(String username, Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count with JOIN to users table
            String countQuery = "SELECT COUNT(*) FROM posts p JOIN users u ON p.user_id = u.id WHERE LOWER(u.username) LIKE LOWER(?)";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                String searchPattern = "%" + username + "%";
                countStmt.setString(1, searchPattern);
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results with JOIN
                    String query = "SELECT p.* FROM posts p JOIN users u ON p.user_id = u.id WHERE LOWER(u.username) LIKE LOWER(?) ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setString(1, searchPattern);
                        stmt.setInt(2, pageable.getPageSize());
                        stmt.setInt(3, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<Post> posts = new ArrayList<>();
                            while (rs.next()) {
                                posts.add(mapResultSetToPost(rs));
                            }
                            return new PageImpl<>(posts, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find posts by author username", e);
        }
    }
}
