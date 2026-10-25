package com.example.store.repository.mapper;

import com.example.store.model.ProductImage;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductImageMapper implements RowMapper<ProductImage> {
    @Override
    public ProductImage mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProductImage(rs.getString("content_type"), rs.getBytes("data"), rs.getTimestamp("updated_at"));
    }
}
