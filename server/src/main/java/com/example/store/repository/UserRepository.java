package com.example.store.repository;

import com.example.store.model.CustomUser;
import com.example.store.repository.mapper.UserMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {
    private static final String USERS_TABLE = "users";
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String register(CustomUser user) {
        String sql = String.format("INSERT INTO %s (first_name, last_name, email, phone, address, username, password, role) VALUES (?,?,?,?,?,?,?,?)", USERS_TABLE);
        jdbcTemplate.update(sql, user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(), user.getAddress(), user.getUsername(), user.getPassword(), user.getRole().name());
        return "User registered successfully";
    }

    public CustomUser findUserByUsername(String username) {
        try {
            String sql = String.format("SELECT * FROM %s WHERE username = ?", USERS_TABLE);
            return jdbcTemplate.queryForObject(sql, new UserMapper(), username);
        } catch (Exception exception) {
            return null;
        }
    }

    public CustomUser findUserByEmail(String email) {
        try {
            String sql = String.format("SELECT * FROM %s WHERE email = ?", USERS_TABLE);
            return jdbcTemplate.queryForObject(sql, new UserMapper(), email);
        } catch (Exception exception) {
            return null;
        }
    }

    public List<CustomUser> findAllUsers() {
        String sql = String.format("SELECT * FROM %s", USERS_TABLE);
        return jdbcTemplate.query(sql, new UserMapper());
    }

    public CustomUser updateUser(CustomUser user) {
        String sql = String.format("UPDATE %s SET first_name = ?, last_name = ?, email = ?, phone = ?, address = ? WHERE username = ?", USERS_TABLE);
        jdbcTemplate.update(sql, user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(), user.getAddress(), user.getUsername());
        return findUserByUsername(user.getUsername());
    }

    public String deleteUser(String username) {
        String sql = String.format("DELETE FROM %s WHERE username = ?", USERS_TABLE);
        jdbcTemplate.update(sql, username);
        return "User deleted successfully";
    }
}
