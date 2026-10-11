package com.example.store.repository.mapper;

import com.example.store.model.Order;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderMapper implements RowMapper<Order> {
    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Order(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getTimestamp("created_at"),
                rs.getBigDecimal("total"),
                rs.getString("status"),
                rs.getString("shipping_address"));
    }
}
