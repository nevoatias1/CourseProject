package com.example.store.repository;

import com.example.store.model.Order;
import com.example.store.repository.mapper.OrderMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class OrderRepository {
    private static final String ORDERS_TABLE = "orders";
    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Order> findAllOrders() {
        return jdbcTemplate.query("SELECT * FROM " + ORDERS_TABLE + " ORDER BY created_at DESC, id DESC", new OrderMapper());
    }

    public Order findOrderById(Integer id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM " + ORDERS_TABLE + " WHERE id = ?", new OrderMapper(), id);
        } catch (Exception exception) {
            return null;
        }
    }

    public List<Order> findOrdersByUsername(String username) {
        return jdbcTemplate.query("SELECT * FROM " + ORDERS_TABLE + " WHERE username = ? ORDER BY created_at DESC, id DESC", new OrderMapper(), username);
    }

    public Order findOrderByIdAndUsername(Integer id, String username) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM " + ORDERS_TABLE + " WHERE id = ? AND username = ?", new OrderMapper(), id, username);
        } catch (Exception exception) {
            return null;
        }
    }

    public Order insertOrder(Order order) {
        String sql = "INSERT INTO " + ORDERS_TABLE + " (username, created_at, total, status, shipping_address) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, order.getUsername());
            statement.setTimestamp(2, order.getCreatedAt());
            statement.setBigDecimal(3, order.getTotal());
            statement.setString(4, order.getStatus());
            statement.setString(5, order.getShippingAddress());
            return statement;
        }, keyHolder);
        order.setId(keyHolder.getKey().intValue());
        return order;
    }

    public Order updateOrder(Order order) {
        jdbcTemplate.update("UPDATE " + ORDERS_TABLE + " SET username = ?, created_at = ?, total = ?, status = ?, shipping_address = ? WHERE id = ?",
                order.getUsername(), order.getCreatedAt(), order.getTotal(), order.getStatus(), order.getShippingAddress(), order.getId());
        return findOrderById(order.getId());
    }

    public String deleteOrder(Integer id) {
        jdbcTemplate.update("DELETE FROM " + ORDERS_TABLE + " WHERE id = ?", id);
        return "Order deleted successfully";
    }
}
