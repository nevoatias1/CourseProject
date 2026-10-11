package com.example.store.controller;

import com.example.store.model.CartItem;
import com.example.store.model.CartRequest;
import com.example.store.model.CartResponse;
import com.example.store.model.QuantityRequest;
import com.example.store.service.CartService;
import com.example.store.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {
    private final CartService cartService;
    private final JwtUtil jwtUtil;

    public CartController(CartService cartService, JwtUtil jwtUtil) {
        this.cartService = cartService;
        this.jwtUtil = jwtUtil;
    }

    private String extractUsername(String token) {
        return jwtUtil.extractUsername(token.substring(7));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(cartService.getCart(extractUsername(token)));
    }

    @PostMapping
    public ResponseEntity<CartItem> addToCart(@RequestHeader("Authorization") String token, @RequestBody CartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(extractUsername(token), request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<CartItem> updateCartQuantity(@RequestHeader("Authorization") String token,
                                                        @PathVariable Integer productId,
                                                        @RequestBody QuantityRequest request) {
        return ResponseEntity.ok(cartService.updateCartQuantity(extractUsername(token), productId, request.getQuantity()));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromCart(@RequestHeader("Authorization") String token, @PathVariable Integer productId) {
        return ResponseEntity.ok(cartService.removeFromCart(extractUsername(token), productId));
    }

    @DeleteMapping
    public ResponseEntity<String> clearCart(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(cartService.clearCart(extractUsername(token)));
    }
}
