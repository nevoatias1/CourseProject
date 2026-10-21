package com.example.store.repository;

import com.example.store.model.Category;
import com.example.store.repository.mapper.CategoryMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

}
