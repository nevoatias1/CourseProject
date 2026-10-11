package com.example.store.controller;

import com.example.store.model.Product;
import com.example.store.service.FavoriteService;
import com.example.store.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = "http://localhost:3000")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final JwtUtil jwtUtil;

    public FavoriteController(FavoriteService favoriteService, JwtUtil jwtUtil) {
        this.favoriteService = favoriteService;
        this.jwtUtil = jwtUtil;
    }

    private String extractUsername(String token) {
        return jwtUtil.extractUsername(token.substring(7));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getFavorites(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(favoriteService.getFavorites(extractUsername(token)));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Product> addFavorite(@RequestHeader("Authorization") String token, @PathVariable Integer productId) {
        return ResponseEntity.ok(favoriteService.addFavorite(extractUsername(token), productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFavorite(@RequestHeader("Authorization") String token, @PathVariable Integer productId) {
        return ResponseEntity.ok(favoriteService.removeFavorite(extractUsername(token), productId));
    }
}
