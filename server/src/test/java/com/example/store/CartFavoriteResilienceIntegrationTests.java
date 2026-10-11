package com.example.store;

import com.example.store.model.CartItem;
import com.example.store.model.CustomUser;
import com.example.store.model.Favorite;
import com.example.store.model.Role;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.FavoriteRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.UserRepository;
import com.example.store.security.CustomUserDetailsService;
import com.example.store.service.ProductService;
import com.example.store.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CartFavoriteResilienceIntegrationTests {
    private static final String USER = "cart_resilience_test";

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private FilterChainProxy springSecurityFilterChain;
    @Autowired private UserRepository userRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductService productService;
    @Autowired private CustomUserDetailsService customUserDetailsService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void prepareData() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain).build();
        jdbcTemplate.update("DELETE FROM cart_items WHERE username = ?", USER);
        jdbcTemplate.update("DELETE FROM favorites WHERE username = ?", USER);
        jdbcTemplate.update("DELETE FROM users WHERE username = ?", USER);
        jdbcTemplate.update("UPDATE products SET active = TRUE WHERE id = 1");
        userRepository.register(new CustomUser("Cart", "Resilience", "cart.resilience@example.test", "0500000000", "Test Address", USER, "not-used", Role.USER));
    }

    @AfterEach
    void cleanData() {
        jdbcTemplate.update("DELETE FROM cart_items WHERE username = ?", USER);
        jdbcTemplate.update("DELETE FROM favorites WHERE username = ?", USER);
        jdbcTemplate.update("DELETE FROM users WHERE username = ?", USER);
        jdbcTemplate.update("UPDATE products SET active = TRUE WHERE id = 1");
    }

    @Test
    void deactivatingProductRemovesCartAndFavoriteReferencesAndReadEndpointsStaySuccessful() throws Exception {
        cartItemRepository.insertCartItem(new CartItem(USER, 1, 1));
        favoriteRepository.insertFavorite(new Favorite(USER, 1));

        productService.deactivateProduct(1);

        mockMvc.perform(get("/cart").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
        mockMvc.perform(get("/favorites").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void removingCartItemLeavesCartReadable() throws Exception {
        cartItemRepository.insertCartItem(new CartItem(USER, productRepository.findProductById(1).getId(), 1));

        mockMvc.perform(delete("/cart/{productId}", 1).header("Authorization", bearerToken()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/cart").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtil.generateToken(customUserDetailsService.loadUserByUsername(USER));
    }
}
