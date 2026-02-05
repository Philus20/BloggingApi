package com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories;

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
public class UserRepository implements Repository<User> {

    private final IConnection connectionService;
    private final ICrudQueries crudQueries;

    @Autowired
    public UserRepository(IConnection connectionService, ICrudQueries crudQueries) {
        this.connectionService = connectionService;
        this.crudQueries = crudQueries;
    }

    /**
     * @param obj
     */
    @Override
    public void create(User obj) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.createQuery("users", "user_name, email, password, role");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, obj.getUsername());
                stmt.setString(2, obj.getEmail());
                stmt.setString(3, obj.getPassword());
                stmt.setString(4, "user");
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * @param id
     * @return
     */
    @Override
    public User findByInteger(int id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.getByIntegerQuery(id, "users", "user_id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
        return null;
    }

    /**
     * @param str
     * @return
     */
    @Override
    public User findByString(String str) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.getStringQuery(str, "users", "user_name");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username", e);
        }
        return null;
    }

    /**
     * @return
     */
    @Override
    public List<User> findAll() {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.getAllQuery("users");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<User> users = new ArrayList<>();
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                    return users;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    /**
     * @param id
     */
    @Override
    public void updateById(int id) {
        // This method is kept for interface compatibility
        // Use update(User obj) for actual updates
        throw new UnsupportedOperationException("Use update(User obj) method instead");
    }
    
    public void update(User obj) {
        try (Connection connection = connectionService.createConnection()) {
            String query = "UPDATE users SET user_name = ?, email = ? WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, obj.getUsername());
                stmt.setString(2, obj.getEmail());
                stmt.setLong(3, obj.getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("user_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        if (rs.getTimestamp("created_at") != null) {
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return user;
    }

    /**
     * @param id
     */
    @Override
    public void delete(int id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.deleteByIdQuery(id, "users", "user_id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    // Additional methods for query services
    public Page<User> findAll(Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count
            String countQuery = "SELECT COUNT(*) FROM users";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results
                    String query = "SELECT * FROM users LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setInt(1, pageable.getPageSize());
                        stmt.setInt(2, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<User> users = new ArrayList<>();
                            while (rs.next()) {
                                users.add(mapResultSetToUser(rs));
                            }
                            return new PageImpl<>(users, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users with pagination", e);
        }
    }

    public Optional<User> findById(Long id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = "SELECT * FROM users WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
        return Optional.empty();
    }

    public Page<User> searchByKeyword(String keyword, Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count
            String countQuery = "SELECT COUNT(*) FROM users WHERE LOWER(user_name) LIKE ? OR LOWER(email) LIKE ?";
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
                    String query = "SELECT * FROM users WHERE LOWER(user_name) LIKE ? OR LOWER(email) LIKE ? LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setString(1, searchPattern);
                        stmt.setString(2, searchPattern);
                        stmt.setInt(3, pageable.getPageSize());
                        stmt.setInt(4, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<User> users = new ArrayList<>();
                            while (rs.next()) {
                                users.add(mapResultSetToUser(rs));
                            }
                            return new PageImpl<>(users, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search users by keyword", e);
        }
    }

    public Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count
            String countQuery = "SELECT COUNT(*) FROM users WHERE LOWER(user_name) LIKE LOWER(?)";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                String searchPattern = "%" + username + "%";
                countStmt.setString(1, searchPattern);
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results
                    String query = "SELECT * FROM users WHERE LOWER(user_name) LIKE LOWER(?) LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setString(1, searchPattern);
                        stmt.setInt(2, pageable.getPageSize());
                        stmt.setInt(3, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<User> users = new ArrayList<>();
                            while (rs.next()) {
                                users.add(mapResultSetToUser(rs));
                            }
                            return new PageImpl<>(users, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find users by username", e);
        }
    }

    public Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable) {
        try (Connection connection = connectionService.createConnection()) {
            // Get total count
            String countQuery = "SELECT COUNT(*) FROM users WHERE LOWER(email) LIKE LOWER(?)";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                String searchPattern = "%" + email + "%";
                countStmt.setString(1, searchPattern);
                try (ResultSet countRs = countStmt.executeQuery()) {
                    long totalElements = 0;
                    if (countRs.next()) {
                        totalElements = countRs.getLong(1);
                    }
                    
                    // Get paginated results
                    String query = "SELECT * FROM users WHERE LOWER(email) LIKE LOWER(?) LIMIT ? OFFSET ?";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setString(1, searchPattern);
                        stmt.setInt(2, pageable.getPageSize());
                        stmt.setInt(3, (int) pageable.getOffset());
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<User> users = new ArrayList<>();
                            while (rs.next()) {
                                users.add(mapResultSetToUser(rs));
                            }
                            return new PageImpl<>(users, pageable, totalElements);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find users by email", e);
        }
    }

    public Optional<User> findByEmailExact(String email) {
        try (Connection connection = connectionService.createConnection()) {
            String query = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
        return Optional.empty();
    }
}
