package com.example.store.repository.mapper;

import com.example.store.model.Product;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductWithCategoryMapper implements RowMapper<Product> {
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        Product product = new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getString("image_url"),
                rs.getInt("stock"),
                rs.getInt("category_id"),
                rs.getString("category_name"));
        product.setActive(rs.getBoolean("active"));
        return product;
    }
}
