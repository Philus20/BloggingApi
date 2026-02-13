package com.example.BloggingApi.Repositories;

import com.example.BloggingApi.Entities.Tag;
import com.example.BloggingApi.DbInterfaces.IConnection;
import com.example.BloggingApi.DbInterfaces.ICrudQueries;
import com.example.BloggingApi.DbInterfaces.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for Tag entity.
 * 
 * <p>This class provides CRUD operations for the Tag entity using JDBC.
 * It implements the Repository interface and includes methods for
 * finding tags by name and managing tag-post relationships.</p>
 */
@org.springframework.stereotype.Repository
public class TagRepository implements Repository<Tag> {

    private final IConnection connectionService;
    private final ICrudQueries crudQueries;

    @Autowired
    public TagRepository(IConnection connectionService, ICrudQueries crudQueries) {
        this.connectionService = connectionService;
        this.crudQueries = crudQueries;
    }

    /**
     * @param obj
     */
    @Override
    public void create(Tag obj) {

    }

    /**
     * @param id
     * @return
     */
    @Override
    public Tag findByInteger(int id) {
        return null;
    }

    /**
     * @param str
     * @return
     */
    @Override
    public Tag findByString(String str) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public List<Tag> findAll() {
        return List.of();
    }

    /**
     * @param id
     */
    @Override
    public void updateById(int id) {

    }

    /**
     * @param id
     */
    @Override
    public void delete(int id) {

    }

    public void delete(Tag tag) {
        try (Connection connection = connectionService.createConnection()) {
            String query = crudQueries.deleteByIdQuery(tag.getId().intValue(), "tags", "tag_id");
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete tag", e);
        }
    }

    // Additional methods for query services
    public Page<Tag> findAll(Pageable pageable) {
        // Implementation needed
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public Optional<Tag> findById(Long id) {
        try (Connection connection = connectionService.createConnection()) {
            String query = "SELECT * FROM tags WHERE tag_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Tag tag = new Tag();
                    tag.setId(rs.getLong("tag_id"));
                    tag.setName(rs.getString("name"));
                    return Optional.of(tag);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tag by id", e);
        }
        return Optional.empty();
    }

    public Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        // Implementation needed
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }
}

