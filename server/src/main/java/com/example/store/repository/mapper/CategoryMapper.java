package com.example.store.repository.mapper;

import com.example.store.model.Category;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryMapper implements RowMapper<Category> {
    @Override
    public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Category(rs.getInt("id"), rs.getString("name"));
    }
}
