package com.example.store.controller;

import com.example.store.model.CheckoutRequest;
import com.example.store.model.Order;
import com.example.store.model.OrderDetails;
import com.example.store.service.OrderService;
import com.example.store.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {
    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    private String extractUsername(String token) {
        return jwtUtil.extractUsername(token.substring(7));
    }

    @PostMapping
    public ResponseEntity<OrderDetails> checkout(@RequestHeader("Authorization") String token,
                                                  @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(extractUsername(token), request.getShippingAddress()));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(orderService.getOrders(extractUsername(token)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetails> getOrder(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrder(extractUsername(token), id));
    }
}
