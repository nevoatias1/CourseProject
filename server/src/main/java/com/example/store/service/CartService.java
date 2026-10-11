package com.example.store.service;

import com.example.store.model.CartItem;
import com.example.store.model.CartItemDetails;
import com.example.store.model.CartResponse;
import com.example.store.model.Product;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getCart(String username) {
        List<CartItemDetails> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItemRepository.findCartItemsByUsername(username)) {
            Product product = productRepository.findPublicProductById(cartItem.getProductId());
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            items.add(new CartItemDetails(product.getId(), product.getName(), product.getPrice(), product.getImageUrl(), cartItem.getQuantity(), lineTotal));
            total = total.add(lineTotal);
        }
        return new CartResponse(items, total);
    }

    public CartItem addToCart(String username, Integer productId, Integer quantity) {
        validateQuantity(quantity);
        Product product = findProduct(productId);
        CartItem existing = cartItemRepository.findCartItemByUsernameAndProductId(username, productId);
        int updatedQuantity = quantity;
        if (existing != null) {
            updatedQuantity += existing.getQuantity();
        }
        validateStock(product, updatedQuantity);
        CartItem cartItem = new CartItem(username, productId, updatedQuantity);
        return existing == null ? cartItemRepository.insertCartItem(cartItem) : cartItemRepository.updateCartItem(cartItem);
    }

    public CartItem updateCartQuantity(String username, Integer productId, Integer quantity) {
        validateQuantity(quantity);
        Product product = findProduct(productId);
        CartItem existing = cartItemRepository.findCartItemByUsernameAndProductId(username, productId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }
        validateStock(product, quantity);
        return cartItemRepository.updateCartItem(new CartItem(username, productId, quantity));
    }

    public String removeFromCart(String username, Integer productId) {
        return cartItemRepository.deleteCartItem(username, productId);
    }

    public String clearCart(String username) {
        return cartItemRepository.deleteCartItemsByUsername(username);
    }

    private Product findProduct(Integer productId) {
        Product product = productRepository.findPublicProductById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return product;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }
    }

    private void validateStock(Product product, Integer quantity) {
        if (quantity > product.getStock()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity exceeds available stock");
        }
    }
}
