package com.example.store.service;

import com.example.store.model.Product;
import com.example.store.repository.CategoryRepository;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.FavoriteRepository;
import com.example.store.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final FavoriteRepository favoriteRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          CartItemRepository cartItemRepository, FavoriteRepository favoriteRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public List<Product> getPublicProducts(Integer categoryId, String search) {
        return productRepository.findPublicProducts(categoryId, search);
    }

    public Product getPublicProductById(Integer id) {
        return productRepository.findPublicProductById(id);
    }

    public List<Product> getAllProducts() { return productRepository.findAllProducts(); }

    public Product createProduct(Product product) {
        validateProduct(product);
        return productRepository.insertProduct(product);
    }

    public Product updateProduct(Integer id, Product product) {
        if (productRepository.findProductById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        validateProduct(product);
        product.setId(id);
        return productRepository.updateProduct(product);
    }

    @Transactional
    public Product deactivateProduct(Integer id) {
        if (productRepository.findProductById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        Product product = productRepository.setProductActive(id, false);
        cartItemRepository.deleteCartItemsByProductId(id);
        favoriteRepository.deleteFavoritesByProductId(id);
        return product;
    }

    public Product restoreProduct(Integer id) {
        if (productRepository.findProductById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return productRepository.setProductActive(id, true);
    }

    private void validateProduct(Product product) {
        if (product == null || product.getName() == null || product.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product price must be greater than 0");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product stock must be at least 0");
        }
        if (product.getCategoryId() == null || categoryRepository.findCategoryById(product.getCategoryId()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product category does not exist");
        }
    }
}
