package com.example.store.repository.mapper;

import com.example.store.model.CartItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CartItemMapper implements RowMapper<CartItem> {
    @Override
    public CartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CartItem(rs.getString("username"), rs.getInt("product_id"), rs.getInt("quantity"));
    }
}
