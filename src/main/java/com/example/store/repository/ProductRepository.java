package com.example.store.repository;

import com.example.store.model.Product;
import com.example.store.repository.mapper.ProductMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ProductRepository {
    private static final String PRODUCTS_TABLE = "products";
    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> findAllProducts() {
        return jdbcTemplate.query("SELECT * FROM " + PRODUCTS_TABLE, new ProductMapper());
    }

    public Product findProductById(Integer id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM " + PRODUCTS_TABLE + " WHERE id = ?", new ProductMapper(), id);
        } catch (Exception exception) {
            return null;
        }
    }

    public List<Product> findProductsByCategoryId(Integer categoryId) {
        return jdbcTemplate.query("SELECT * FROM " + PRODUCTS_TABLE + " WHERE category_id = ?", new ProductMapper(), categoryId);
    }

    public Product insertProduct(Product product) {
        String sql = "INSERT INTO " + PRODUCTS_TABLE + " (name, description, price, image_url, stock, category_id) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setString(4, product.getImageUrl());
            statement.setInt(5, product.getStock());
            statement.setInt(6, product.getCategoryId());
            return statement;
        }, keyHolder);
        product.setId(keyHolder.getKey().intValue());
        return product;
    }

    public Product updateProduct(Product product) {
        jdbcTemplate.update("UPDATE " + PRODUCTS_TABLE + " SET name = ?, description = ?, price = ?, image_url = ?, stock = ?, category_id = ? WHERE id = ?",
                product.getName(), product.getDescription(), product.getPrice(), product.getImageUrl(), product.getStock(), product.getCategoryId(), product.getId());
        return findProductById(product.getId());
    }

    public String deleteProduct(Integer id) {
        jdbcTemplate.update("DELETE FROM " + PRODUCTS_TABLE + " WHERE id = ?", id);
        return "Product deleted successfully";
    }
}
