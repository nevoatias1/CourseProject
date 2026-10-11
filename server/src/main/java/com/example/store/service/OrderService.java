package com.example.store.service;

import com.example.store.model.CartItem;
import com.example.store.model.Order;
import com.example.store.model.OrderDetails;
import com.example.store.model.OrderItem;
import com.example.store.model.OrderItemDetails;
import com.example.store.model.Product;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.OrderItemRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(CartItemRepository cartItemRepository, ProductRepository productRepository,
                        OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderDetails checkout(String username, String shippingAddress) {
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shipping address is required");
        }

        List<CartItem> cartItems = cartItemRepository.findCartItemsByUsername(username);
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        List<CheckoutItem> checkoutItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findProductById(cartItem.getProductId());
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            }
            checkoutItems.add(new CheckoutItem(product, cartItem.getQuantity()));
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        for (CheckoutItem item : checkoutItems) {
            if (productRepository.decrementStockIfAvailable(item.product().getId(), item.quantity()) == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock for product: " + item.product().getName());
            }
        }

        Order order = orderRepository.insertOrder(new Order(null, username, new Timestamp(System.currentTimeMillis()),
                total, "PLACED", shippingAddress.trim()));
        List<OrderItemDetails> itemDetails = new ArrayList<>();
        for (CheckoutItem item : checkoutItems) {
            orderItemRepository.insertOrderItem(new OrderItem(order.getId(), item.product().getId(), item.quantity(), item.product().getPrice()));
            itemDetails.add(new OrderItemDetails(item.product().getId(), item.product().getName(), item.quantity(),
                    item.product().getPrice(), item.product().getPrice().multiply(BigDecimal.valueOf(item.quantity()))));
        }
        cartItemRepository.deleteCartItemsByUsername(username);
        return toDetails(order, itemDetails);
    }

    public List<Order> getOrders(String username) {
        return orderRepository.findOrdersByUsername(username);
    }

    public OrderDetails getOrder(String username, Integer orderId) {
        Order order = orderRepository.findOrderByIdAndUsername(orderId, username);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }

        List<OrderItemDetails> itemDetails = new ArrayList<>();
        for (OrderItem item : orderItemRepository.findOrderItemsByOrderId(orderId)) {
            Product product = productRepository.findProductById(item.getProductId());
            itemDetails.add(new OrderItemDetails(item.getProductId(), product.getName(), item.getQuantity(), item.getUnitPrice(),
                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        }
        return toDetails(order, itemDetails);
    }

    private OrderDetails toDetails(Order order, List<OrderItemDetails> itemDetails) {
        return new OrderDetails(order.getId(), order.getCreatedAt(), order.getTotal(), order.getStatus(), order.getShippingAddress(), itemDetails);
    }

    private record CheckoutItem(Product product, Integer quantity) { }
}
