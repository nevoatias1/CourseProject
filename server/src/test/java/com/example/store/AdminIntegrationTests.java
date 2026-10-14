package com.example.store;

import com.example.store.model.CartItem;
import com.example.store.model.CustomUser;
import com.example.store.model.OrderDetails;
import com.example.store.model.Product;
import com.example.store.model.Role;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.UserRepository;
import com.example.store.security.CustomUserDetailsService;
import com.example.store.service.OrderService;
import com.example.store.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
class AdminIntegrationTests {
    private static final String ADMIN = "admin_stage9_test";
    private static final String USER = "user_stage9_test";

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private FilterChainProxy springSecurityFilterChain;
    @Autowired private UserRepository userRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderService orderService;
    @Autowired private CustomUserDetailsService customUserDetailsService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;
    private int productOneStock;
    private int productTwoStock;

    @BeforeEach
    void prepareData() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain).build();
        jdbcTemplate.update("DELETE FROM order_items");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM cart_items");
        jdbcTemplate.update("DELETE FROM users WHERE username IN (?, ?)", ADMIN, USER);
        jdbcTemplate.update("UPDATE products SET active = TRUE WHERE id IN (1, 2)");
        productOneStock = productRepository.findProductById(1).getStock();
        productTwoStock = productRepository.findProductById(2).getStock();
        registerUser(ADMIN, Role.ADMIN);
        registerUser(USER, Role.USER);
    }

    @AfterEach
    void cleanData() {
        jdbcTemplate.update("DELETE FROM order_items");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM cart_items");
        jdbcTemplate.update("DELETE FROM users WHERE username IN (?, ?)", ADMIN, USER);
        jdbcTemplate.update("UPDATE products SET active = TRUE, stock = ? WHERE id = 1", productOneStock);
        jdbcTemplate.update("UPDATE products SET active = TRUE, stock = ? WHERE id = 2", productTwoStock);
    }

    @Test
    void userTokenIsForbiddenFromNewAdminEndpoints() throws Exception {
        mockMvc.perform(get("/admin/products").header("Authorization", bearerToken(USER))).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/orders").header("Authorization", bearerToken(USER))).andExpect(status().isForbidden());
        mockMvc.perform(put("/admin/products/1/restore").header("Authorization", bearerToken(USER))).andExpect(status().isForbidden());
    }

    @Test
    void softDeletedProductIsHiddenFromPublicAndCartButRemainsInOrderHistory() throws Exception {
        Product product = productRepository.findProductById(1);
        cartItemRepository.insertCartItem(new CartItem(USER, product.getId(), 1));
        OrderDetails order = orderService.checkout(USER, "1 History Street");

        mockMvc.perform(delete("/admin/products/{id}", product.getId()).header("Authorization", bearerToken(ADMIN)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/products/{id}", product.getId())).andExpect(status().isNotFound());
        mockMvc.perform(post("/cart").header("Authorization", bearerToken(USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":1}"))
                .andExpect(status().isNotFound());

        OrderDetails historicalOrder = orderService.getOrder(USER, order.getId());
        assertEquals(product.getName(), historicalOrder.getItems().getFirst().getProductName());
    }

    @Test
    void cancellingOrderRestoresStockAndInvalidTransitionReturnsBadRequest() throws Exception {
        Product product = productRepository.findProductById(2);
        cartItemRepository.insertCartItem(new CartItem(USER, product.getId(), 2));
        OrderDetails order = orderService.checkout(USER, "2 Cancel Street");
        assertEquals(product.getStock() - 2, productRepository.findProductById(product.getId()).getStock());

        mockMvc.perform(put("/admin/orders/{id}/status", order.getId()).header("Authorization", bearerToken(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk());
        assertEquals(product.getStock(), productRepository.findProductById(product.getId()).getStock());
        mockMvc.perform(put("/admin/orders/{id}/status", order.getId()).header("Authorization", bearerToken(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid order status transition"));
    }

    @Test
    void adminCannotChangeOwnRoleOrDeleteOwnAccount() throws Exception {
        mockMvc.perform(put("/admin/users/{username}/role", ADMIN).header("Authorization", bearerToken(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"USER\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/admin/delete-user/{username}", ADMIN).header("Authorization", bearerToken(ADMIN)))
                .andExpect(status().isBadRequest());
        assertNotNull(userRepository.findUserByUsername(ADMIN));
        assertEquals(Role.ADMIN, userRepository.findUserByUsername(ADMIN).getRole());
    }

    private void registerUser(String username, Role role) {
        userRepository.register(new CustomUser("Stage", "Nine", username + "@example.test", "0500000000",
                "Test Address", username, "not-used-in-this-test", role));
    }

    private String bearerToken(String username) {
        return "Bearer " + jwtUtil.generateToken(customUserDetailsService.loadUserByUsername(username));
    }
}
