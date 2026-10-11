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

    public List<OrderItem> findAllOrderItems() {
        return jdbcTemplate.query("SELECT * FROM " + ORDER_ITEMS_TABLE, new OrderItemMapper());
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

    public OrderItem updateOrderItem(OrderItem orderItem) {
        jdbcTemplate.update("UPDATE " + ORDER_ITEMS_TABLE + " SET quantity = ?, unit_price = ? WHERE order_id = ? AND product_id = ?",
                orderItem.getQuantity(), orderItem.getUnitPrice(), orderItem.getOrderId(), orderItem.getProductId());
        return findOrderItemByOrderIdAndProductId(orderItem.getOrderId(), orderItem.getProductId());
    }

    public String deleteOrderItem(Integer orderId, Integer productId) {
        jdbcTemplate.update("DELETE FROM " + ORDER_ITEMS_TABLE + " WHERE order_id = ? AND product_id = ?", orderId, productId);
        return "Order item deleted successfully";
    }
}
