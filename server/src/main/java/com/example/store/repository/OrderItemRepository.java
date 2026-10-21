package com.example.store.repository;

import com.example.store.model.OrderItem;
import com.example.store.repository.mapper.OrderItemMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemRepository {
    private static final String ORDER_ITEMS_TABLE = "order_items";
    private final JdbcTemplate jdbcTemplate;

    public OrderItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrderItem> findOrderItemsByOrderId(Integer orderId) {
        return jdbcTemplate.query("SELECT * FROM " + ORDER_ITEMS_TABLE + " WHERE order_id = ?", new OrderItemMapper(), orderId);
    }

    public OrderItem findOrderItemByOrderIdAndProductId(Integer orderId, Integer productId) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM " + ORDER_ITEMS_TABLE + " WHERE order_id = ? AND product_id = ?",
                    new OrderItemMapper(), orderId, productId);
        } catch (Exception exception) {
            return null;
        }
    }

    public OrderItem insertOrderItem(OrderItem orderItem) {
        jdbcTemplate.update("INSERT INTO " + ORDER_ITEMS_TABLE + " (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                orderItem.getOrderId(), orderItem.getProductId(), orderItem.getQuantity(), orderItem.getUnitPrice());
        return orderItem;
    }

}
