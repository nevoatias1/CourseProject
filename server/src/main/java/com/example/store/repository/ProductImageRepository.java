package com.example.store.repository;

import com.example.store.model.ProductImage;
import com.example.store.repository.mapper.ProductImageMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class ProductImageRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProductImageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ProductImage findByProductId(Integer productId) {
        try {
            return jdbcTemplate.queryForObject("SELECT content_type, data, updated_at FROM product_images WHERE product_id = ?",
                    new ProductImageMapper(), productId);
        } catch (Exception exception) {
            return null;
        }
    }

    public void save(Integer productId, String contentType, byte[] data, Timestamp updatedAt) {
        int updated = jdbcTemplate.update("UPDATE product_images SET content_type = ?, data = ?, updated_at = ? WHERE product_id = ?",
                contentType, data, updatedAt, productId);
        if (updated == 0) {
            jdbcTemplate.update("INSERT INTO product_images (product_id, content_type, data, updated_at) VALUES (?, ?, ?, ?)",
                    productId, contentType, data, updatedAt);
        }
    }

    public void deleteByProductId(Integer productId) {
        jdbcTemplate.update("DELETE FROM product_images WHERE product_id = ?", productId);
    }
}
