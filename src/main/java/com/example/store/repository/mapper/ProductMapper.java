package com.example.store.repository.mapper;

import com.example.store.model.Product;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMapper implements RowMapper<Product> {
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getString("image_url"),
                rs.getInt("stock"),
                rs.getInt("category_id"));
    }
}
