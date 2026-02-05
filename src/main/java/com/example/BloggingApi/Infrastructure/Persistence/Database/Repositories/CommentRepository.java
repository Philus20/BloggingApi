package com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories;

import com.example.BloggingApi.Domain.Entities.Comment;
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
public class CommentRepository implements Repository<Comment> {

    private final IConnection connectionService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private ICrudQueries crudQueries;


    @Autowired
    public CommentRepository(IConnection connectionService, UserRepository userRepository, PostRepository postRepository, ICrudQueries crudQueries) {
        this.connectionService = connectionService;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.crudQueries = crudQueries;
    }


    @Override
    public void create(Comment obj) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.createQuery("comments", "content, post_id, user_id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, obj.getContent());
                stmt.setLong(2, obj.getPost().getId());
                stmt.setLong(3, obj.getAuthor().getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create comment", e);
        }
    }

    @Override
    public Comment findByInteger(int id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.getByIntegerQuery(id, "comments", "comment_id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToComment(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comment by id", e);
        }
        return null;
    }

    @Override
    public Comment findByString(String str) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.getStringQuery(str, "comments", "content");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToComment(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comment by content", e);
        }
        return null;
    }

    @Override
    public List<Comment> findAll() {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.getAllQuery("comments");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Comment> comments = new ArrayList<>();
                    while (rs.next()) {
                        comments.add(mapResultSetToComment(rs));
                    }
                    return comments;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all comments", e);
        }
    }

    @Override
    public void updateById(int id) {
        // This method is kept for interface compatibility
        // Use update(Comment obj) for actual updates
        throw new UnsupportedOperationException("Use update(Comment obj) method instead");
    }
    
    public void update(Comment obj) {
        try (Connection connection = connectionService.createConnection()) {
            String query = "UPDATE comments SET content = ? WHERE comment_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, obj.getContent());
                stmt.setLong(2, obj.getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update comment", e);
        }
    }
    
    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getLong("comment_id"));
        comment.setContent(rs.getString("content"));
        
        // Set post if post_id is available
        int postId = rs.getInt("post_id");
        if (!rs.wasNull()) {
            Post post = postRepository.findByInteger(postId);
            comment.setPost(post);
        }
        
        // Set author if author_id is available
        int authorId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            User author = userRepository.findByInteger(authorId);
            comment.setAuthor(author);
        }
        
        // Set created_at if available
        if (rs.getTimestamp("created_at") != null) {
            comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        
        return comment;
    }

    @Override
    public void delete(int id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.deleteByIdQuery(id, "comments", "comment_id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete comment", e);
        }
    }

    // Additional methods for query services
    public Page<Comment> findAll(Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count
            String countQuery = "SELECT COUNT(*) FROM comments";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results
                    String query = "SELECT * FROM comments ORDER BY created_at DESC LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setInt(1, pageable.getPageSize());
                        stmt.setInt(2, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<Comment> comments = new ArrayList<>();
                            while (rs.next()) {
                                comments.add(mapResultSetToComment(rs));
                            }
                            return new PageImpl<>(comments, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all comments with pagination", e);
        }
    }

    public Optional<Comment> findById(Long id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = "SELECT * FROM comments WHERE comment_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToComment(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comment by id", e);
        }
        return Optional.empty();
    }

    public Page<Comment> findByContentContainingIgnoreCase(String content, Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count
            String countQuery = "SELECT COUNT(*) FROM comments WHERE LOWER(content) LIKE LOWER(?)";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                String searchPattern = "%" + content + "%";
                countStmt.setString(1, searchPattern);
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results
                    String query = "SELECT * FROM comments WHERE LOWER(content) LIKE LOWER(?) ORDER BY created_at DESC LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setString(1, searchPattern);
                        stmt.setInt(2, pageable.getPageSize());
                        stmt.setInt(3, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<Comment> comments = new ArrayList<>();
                            while (rs.next()) {
                                comments.add(mapResultSetToComment(rs));
                            }
                            return new PageImpl<>(comments, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comments by content", e);
        }
    }

    public Page<Comment> findByAuthorUsernameContainingIgnoreCase(String username, Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count with JOIN to users table
            String countQuery = "SELECT COUNT(*) FROM comments c JOIN users u ON c.user_id = u.id WHERE LOWER(u.username) LIKE LOWER(?)";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                String searchPattern = "%" + username + "%";
                countStmt.setString(1, searchPattern);
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results with JOIN
                    String query = "SELECT c.* FROM comments c JOIN users u ON c.user_id = u.id WHERE LOWER(u.username) LIKE LOWER(?) ORDER BY c.created_at DESC LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setString(1, searchPattern);
                        stmt.setInt(2, pageable.getPageSize());
                        stmt.setInt(3, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<Comment> comments = new ArrayList<>();
                            while (rs.next()) {
                                comments.add(mapResultSetToComment(rs));
                            }
                            return new PageImpl<>(comments, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comments by author username", e);
        }
    }
}
