package com.javawallet.application.ports;

import com.javawallet.domain.model.Category;
import com.javawallet.infrastructure.persistence.MariaDbConnectionManager;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class MariaDbCategoryRepository implements ICategoryRepository {

    private final MariaDbConnectionManager connectionManager;

    public MariaDbCategoryRepository(MariaDbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    // ---------------------------------------------------------------
    // SAVE (insert or update category)
    // ---------------------------------------------------------------
    @Override
    public void save(Category category) {
        String sql = "INSERT INTO categories (id, name, parent_id) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), parent_id = VALUES(parent_id)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getId().toString());
            ps.setString(2, category.getName());
            ps.setString(3, category.getParent() != null ? category.getParent().getId().toString() : null);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save category " + category.getId(), e);
        }
    }

    // ---------------------------------------------------------------
    // LOAD ALL CATEGORIES (with parent-child relationships wired up)
    // ---------------------------------------------------------------
    @Override
    public Collection<Category> loadCategories() {
        String sql = "SELECT id, name, parent_id FROM categories";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // First pass: create all category objects
            Map<UUID, Category> categoryMap = new LinkedHashMap<>();
            List<CategoryRow> rows = new ArrayList<>();

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                String name = rs.getString("name");
                String parentIdStr = rs.getString("parent_id");
                UUID parentId = parentIdStr != null ? UUID.fromString(parentIdStr) : null;

                Category category = new Category(name);
                setId(category, id);

                categoryMap.put(id, category);
                rows.add(new CategoryRow(id, parentId, category));
            }

            // Second pass: wire up parent-child relationships
            for (CategoryRow row : rows) {
                if (row.parentId != null) {
                    Category parent = categoryMap.get(row.parentId);
                    if (parent != null) {
                        setParent(row.category, parent);
                        addChild(parent, row.category);
                    }
                }
            }

            return new ArrayList<>(categoryMap.values());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load categories", e);
        }
    }

    // ---------------------------------------------------------------
    // LOAD SUBCATEGORIES BY PARENT ID
    // ---------------------------------------------------------------
    @Override
    public Collection<Category> loadSubcategories(UUID parentId) {
        String sql = "SELECT c.id, c.name, p.name AS parent_name " +
                     "FROM categories c " +
                     "JOIN categories p ON p.id = c.parent_id " +
                     "WHERE c.parent_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, parentId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<Category> subcategories = new ArrayList<>();
                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    String name = rs.getString("name");
                    String parentName = rs.getString("parent_name");

                    Category category = new Category(name);
                    setId(category, id);

                    // Build a parent reference with the real name so getParent().getName() works
                    Category parent = new Category(parentName);
                    setId(parent, parentId);
                    setParent(category, parent);

                    subcategories.add(category);
                }
                return subcategories;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load subcategories for parent " + parentId, e);
        }
    }

    // ---------------------------------------------------------------
    // REMOVE CATEGORY BY ID
    // ---------------------------------------------------------------
    @Override
    public void remove(UUID id) {
        // First, detach children by setting their parent_id to NULL
        String detachChildrenSql = "UPDATE categories SET parent_id = NULL WHERE parent_id = ?";
        String deleteSql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDetach = conn.prepareStatement(detachChildrenSql);
                 PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {

                psDetach.setString(1, id.toString());
                psDetach.executeUpdate();

                psDelete.setString(1, id.toString());
                psDelete.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Failed to remove category " + id, e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Connection error while removing category " + id, e);
        }
    }

    // =================================================================
    //  PRIVATE REFLECTION HELPERS
    // =================================================================

    private void setId(Category category, UUID id) {
        try {
            Field idField = Category.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(category, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set category ID", e);
        }
    }

    private void setParent(Category category, Category parent) {
        try {
            Field parentField = Category.class.getDeclaredField("parent");
            parentField.setAccessible(true);
            parentField.set(category, parent);
        } catch (Exception e) {
            throw new RuntimeException("Could not set category parent", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void addChild(Category parent, Category child) {
        try {
            Field childrenField = Category.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            Collection<Category> children = (Collection<Category>) childrenField.get(parent);
            children.add(child);
        } catch (Exception e) {
            throw new RuntimeException("Could not add child to category", e);
        }
    }

    // =================================================================
    //  INTERNAL RECORD
    // =================================================================

    private record CategoryRow(UUID id, UUID parentId, Category category) {}
}
