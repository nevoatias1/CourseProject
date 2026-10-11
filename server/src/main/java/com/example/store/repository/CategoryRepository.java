package com.example.store.repository;

import com.example.store.model.Category;
import com.example.store.repository.mapper.CategoryMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CategoryRepository {
    private static final String CATEGORIES_TABLE = "categories";
    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Category> findAllCategories() {
        return jdbcTemplate.query("SELECT * FROM " + CATEGORIES_TABLE, new CategoryMapper());
    }

    public Category findCategoryById(Integer id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM " + CATEGORIES_TABLE + " WHERE id = ?", new CategoryMapper(), id);
        } catch (Exception exception) {
            return null;
        }
    }

    public Category insertCategory(Category category) {
        String sql = "INSERT INTO " + CATEGORIES_TABLE + " (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, category.getName());
            return statement;
        }, keyHolder);
        category.setId(keyHolder.getKey().intValue());
        return category;
    }

    public Category updateCategory(Category category) {
        jdbcTemplate.update("UPDATE " + CATEGORIES_TABLE + " SET name = ? WHERE id = ?", category.getName(), category.getId());
        return findCategoryById(category.getId());
    }

    public String deleteCategory(Integer id) {
        jdbcTemplate.update("DELETE FROM " + CATEGORIES_TABLE + " WHERE id = ?", id);
        return "Category deleted successfully";
    }
}
