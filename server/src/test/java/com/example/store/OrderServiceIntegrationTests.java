package com.example.store;

import com.example.store.model.CartItem;
import com.example.store.model.CustomUser;
import com.example.store.model.OrderDetails;
import com.example.store.model.Product;
import com.example.store.model.Role;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.OrderItemRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.UserRepository;
import com.example.store.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrderServiceIntegrationTests {
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanOrderTestData() {
        jdbcTemplate.update("DELETE FROM order_items");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM cart_items");
        jdbcTemplate.update("DELETE FROM users WHERE username LIKE 'order_test_%'");
    }

    @Test
    void successfulCheckoutCreatesOrderDecrementsStockAndClearsCart() {
        String username = "order_test_success";
        registerUser(username);
        Product productBeforeCheckout = productRepository.findProductById(1);
        cartItemRepository.insertCartItem(new CartItem(username, productBeforeCheckout.getId(), 2));

        OrderDetails order = orderService.checkout(username, "1 Checkout Street");

        assertNotNull(order.getId());
        assertEquals("PLACED", order.getStatus());
        assertEquals(1, order.getItems().size());
        assertEquals(0, order.getTotal().compareTo(productBeforeCheckout.getPrice().multiply(BigDecimal.valueOf(2))));
        assertEquals(1, orderRepository.findOrdersByUsername(username).size());
        assertNotNull(orderItemRepository.findOrderItemByOrderIdAndProductId(order.getId(), productBeforeCheckout.getId()));
        assertEquals(productBeforeCheckout.getStock() - 2, productRepository.findProductById(productBeforeCheckout.getId()).getStock());
        assertTrue(cartItemRepository.findCartItemsByUsername(username).isEmpty());
    }

    @Test
    void insufficientStockRollsBackOrderStockAndCartChanges() {
        String username = "order_test_rollback";
        registerUser(username);
        Product productBeforeCheckout = productRepository.findProductById(2);
        int cartQuantity = productBeforeCheckout.getStock() + 1;
        cartItemRepository.insertCartItem(new CartItem(username, productBeforeCheckout.getId(), cartQuantity));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.checkout(username, "2 Rollback Street"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains(productBeforeCheckout.getName()));
        assertTrue(orderRepository.findOrdersByUsername(username).isEmpty());
        assertEquals(productBeforeCheckout.getStock(), productRepository.findProductById(productBeforeCheckout.getId()).getStock());
        assertEquals(cartQuantity, cartItemRepository.findCartItemByUsernameAndProductId(username, productBeforeCheckout.getId()).getQuantity());
    }

    @Test
    void anotherUserCannotReadAnOrder() {
        String userA = "order_test_user_a";
        String userB = "order_test_user_b";
        registerUser(userA);
        registerUser(userB);
        cartItemRepository.insertCartItem(new CartItem(userA, 3, 1));
        OrderDetails order = orderService.checkout(userA, "3 Privacy Street");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.getOrder(userB, order.getId()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private void registerUser(String username) {
        userRepository.register(new CustomUser("Order", "Test", username + "@example.test", "0500000000",
                "Test Address", username, "not-used-in-this-test", Role.USER));
    }
}
