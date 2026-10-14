package com.example.store.repository;

import com.example.store.model.Product;
import com.example.store.repository.mapper.ProductMapper;
import com.example.store.repository.mapper.ProductWithCategoryMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;

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

    public List<Product> findPublicProducts(Integer categoryId, String search) {
        StringBuilder sql = new StringBuilder("SELECT p.id, p.name, p.description, p.price, p.image_url, p.stock, p.category_id, p.active, c.name AS category_name "
                + "FROM products p JOIN categories c ON p.category_id = c.id WHERE p.active = TRUE");
        List<Object> parameters = new ArrayList<>();
        if (categoryId != null || (search != null && !search.isBlank())) {
            sql.append(" AND ");
            if (categoryId != null) {
                sql.append("p.category_id = ?");
                parameters.add(categoryId);
            }
            if (search != null && !search.isBlank()) {
                if (categoryId != null) {
                    sql.append(" AND ");
                }
                sql.append("LOWER(p.name) LIKE LOWER(?)");
                parameters.add("%" + search + "%");
            }
        }
        sql.append(" ORDER BY p.id");
        return jdbcTemplate.query(sql.toString(), new ProductWithCategoryMapper(), parameters.toArray());
    }

    public Product findPublicProductById(Integer id) {
        try {
            String sql = "SELECT p.id, p.name, p.description, p.price, p.image_url, p.stock, p.category_id, p.active, c.name AS category_name "
                    + "FROM products p JOIN categories c ON p.category_id = c.id WHERE p.id = ? AND p.active = TRUE";
            return jdbcTemplate.queryForObject(sql, new ProductWithCategoryMapper(), id);
        } catch (Exception exception) {
            return null;
        }
    }

    public Product insertProduct(Product product) {
        String sql = "INSERT INTO " + PRODUCTS_TABLE + " (name, description, price, image_url, stock, category_id, active) VALUES (?, ?, ?, ?, ?, ?, TRUE)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setString(4, product.getImageUrl());
            statement.setInt(5, product.getStock());
            statement.setInt(6, product.getCategoryId());
            return statement;
        }, keyHolder);
        product.setId(GeneratedKeySupport.requiredId(keyHolder));
        return product;
    }

    public Product updateProduct(Product product) {
        jdbcTemplate.update("UPDATE " + PRODUCTS_TABLE + " SET name = ?, description = ?, price = ?, image_url = ?, stock = ?, category_id = ? WHERE id = ?",
                product.getName(), product.getDescription(), product.getPrice(), product.getImageUrl(), product.getStock(), product.getCategoryId(), product.getId());
        return findProductById(product.getId());
    }

    public int decrementStockIfAvailable(Integer productId, Integer quantity) {
        return jdbcTemplate.update("UPDATE " + PRODUCTS_TABLE + " SET stock = stock - ? WHERE id = ? AND stock >= ? AND active = TRUE",
                quantity, productId, quantity);
    }

    public int incrementStock(Integer productId, Integer quantity) {
        return jdbcTemplate.update("UPDATE " + PRODUCTS_TABLE + " SET stock = stock + ? WHERE id = ?", quantity, productId);
    }

    public Product setProductActive(Integer id, boolean active) {
        jdbcTemplate.update("UPDATE " + PRODUCTS_TABLE + " SET active = ? WHERE id = ?", active, id);
        return findProductById(id);
    }

    public String deleteProduct(Integer id) {
        setProductActive(id, false);
        return "Product deactivated successfully";
    }
}
