package com.example.store.service;

import com.example.store.model.Favorite;
import com.example.store.model.Product;
import com.example.store.repository.FavoriteRepository;
import com.example.store.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, ProductRepository productRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
    }

    public List<Product> getFavorites(String username) {
        List<Product> favorites = new ArrayList<>();
        for (Favorite favorite : favoriteRepository.findFavoritesByUsername(username)) {
            Product product = productRepository.findPublicProductById(favorite.getProductId());
            if (product != null) {
                favorites.add(product);
            }
        }
        return favorites;
    }

    public Product addFavorite(String username, Integer productId) {
        Product product = findProduct(productId);
        if (favoriteRepository.findFavoriteByUsernameAndProductId(username, productId) == null) {
            favoriteRepository.insertFavorite(new Favorite(username, productId));
        }
        return product;
    }

    public String removeFavorite(String username, Integer productId) {
        return favoriteRepository.deleteFavorite(username, productId);
    }

    private Product findProduct(Integer productId) {
        Product product = productRepository.findPublicProductById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return product;
    }
}
