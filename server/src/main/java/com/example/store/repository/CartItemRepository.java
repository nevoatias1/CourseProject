package com.example.store.repository;

import com.example.store.model.CartItem;
import com.example.store.repository.mapper.CartItemMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartItemRepository {
    private static final String CART_ITEMS_TABLE = "cart_items";
    private final JdbcTemplate jdbcTemplate;

    public CartItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CartItem> findAllCartItems() {
        return jdbcTemplate.query("SELECT * FROM " + CART_ITEMS_TABLE, new CartItemMapper());
    }

    public List<CartItem> findCartItemsByUsername(String username) {
        return jdbcTemplate.query("SELECT * FROM " + CART_ITEMS_TABLE + " WHERE username = ?", new CartItemMapper(), username);
    }

    public CartItem findCartItemByUsernameAndProductId(String username, Integer productId) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM " + CART_ITEMS_TABLE + " WHERE username = ? AND product_id = ?", new CartItemMapper(), username, productId);
        } catch (Exception exception) {
            return null;
        }
    }

    public CartItem insertCartItem(CartItem cartItem) {
        jdbcTemplate.update("INSERT INTO " + CART_ITEMS_TABLE + " (username, product_id, quantity) VALUES (?, ?, ?)",
                cartItem.getUsername(), cartItem.getProductId(), cartItem.getQuantity());
        return cartItem;
    }

    public CartItem updateCartItem(CartItem cartItem) {
        jdbcTemplate.update("UPDATE " + CART_ITEMS_TABLE + " SET quantity = ? WHERE username = ? AND product_id = ?",
                cartItem.getQuantity(), cartItem.getUsername(), cartItem.getProductId());
        return findCartItemByUsernameAndProductId(cartItem.getUsername(), cartItem.getProductId());
    }

    public String deleteCartItem(String username, Integer productId) {
        jdbcTemplate.update("DELETE FROM " + CART_ITEMS_TABLE + " WHERE username = ? AND product_id = ?", username, productId);
        return "Cart item deleted successfully";
    }

    public String deleteCartItemsByUsername(String username) {
        jdbcTemplate.update("DELETE FROM " + CART_ITEMS_TABLE + " WHERE username = ?", username);
        return "Cart cleared successfully";
    }
}
