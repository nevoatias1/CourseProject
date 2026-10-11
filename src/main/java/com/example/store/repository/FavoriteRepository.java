package com.example.store.repository;

import com.example.store.model.Favorite;
import com.example.store.repository.mapper.FavoriteMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FavoriteRepository {
    private static final String FAVORITES_TABLE = "favorites";
    private final JdbcTemplate jdbcTemplate;

    public FavoriteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Favorite> findAllFavorites() {
        return jdbcTemplate.query("SELECT * FROM " + FAVORITES_TABLE, new FavoriteMapper());
    }

    public List<Favorite> findFavoritesByUsername(String username) {
        return jdbcTemplate.query("SELECT * FROM " + FAVORITES_TABLE + " WHERE username = ?", new FavoriteMapper(), username);
    }

    public Favorite findFavoriteByUsernameAndProductId(String username, Integer productId) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM " + FAVORITES_TABLE + " WHERE username = ? AND product_id = ?", new FavoriteMapper(), username, productId);
        } catch (Exception exception) {
            return null;
        }
    }

    public Favorite insertFavorite(Favorite favorite) {
        jdbcTemplate.update("INSERT INTO " + FAVORITES_TABLE + " (username, product_id) VALUES (?, ?)", favorite.getUsername(), favorite.getProductId());
        return favorite;
    }

    public String deleteFavorite(String username, Integer productId) {
        jdbcTemplate.update("DELETE FROM " + FAVORITES_TABLE + " WHERE username = ? AND product_id = ?", username, productId);
        return "Favorite deleted successfully";
    }
}
