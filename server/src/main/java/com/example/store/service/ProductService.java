package com.example.store.service;

import com.example.store.model.Product;
import com.example.store.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getPublicProducts(Integer categoryId, String search) {
        return productRepository.findPublicProducts(categoryId, search);
    }

    public Product getPublicProductById(Integer id) {
        return productRepository.findPublicProductById(id);
    }
}
