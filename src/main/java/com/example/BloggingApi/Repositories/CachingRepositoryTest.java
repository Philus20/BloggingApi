package com.example.BloggingApi.Repositories;

import com.example.BloggingApi.DbInterfaces.IConnection;
import com.example.BloggingApi.DbInterfaces.ICrudQueries;
import com.example.BloggingApi.DbInterfaces.Repository;
import com.example.BloggingApi.Entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
@org.springframework.stereotype.Repository
public class CachingRepositoryTest implements Repository<User> {

    private final IConnection connectionService;
    private final ICrudQueries crudQueries;

    @Autowired
    public CachingRepositoryTest(IConnection connectionService, ICrudQueries crudQueries) {
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
    public void delete(int userId) {
        try (Connection connection = connectionService.createConnection()) {
            connection.setAutoCommit(false); // start transaction

            String deleteComments = "DELETE FROM comments WHERE post_id IN (SELECT post_id FROM posts WHERE user_id = ?)";
            try (PreparedStatement stmt = connection.prepareStatement(deleteComments)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            String deletePosts = "DELETE FROM posts WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deletePosts)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            String deleteUser = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteUser)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            connection.commit(); // commit transaction
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

}