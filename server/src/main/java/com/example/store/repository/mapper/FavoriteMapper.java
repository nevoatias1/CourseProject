package com.example.store.repository.mapper;

import com.example.store.model.Favorite;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoriteMapper implements RowMapper<Favorite> {
    @Override
    public Favorite mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Favorite(rs.getString("username"), rs.getInt("product_id"));
    }
}
