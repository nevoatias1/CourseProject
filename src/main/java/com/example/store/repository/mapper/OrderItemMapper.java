package com.example.store.repository.mapper;

import com.example.store.model.OrderItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderItemMapper implements RowMapper<OrderItem> {
    @Override
    public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new OrderItem(
                rs.getInt("order_id"),
                rs.getInt("product_id"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price"));
    }
}
