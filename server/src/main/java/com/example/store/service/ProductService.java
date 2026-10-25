package com.example.store.service;

import com.example.store.model.Product;
import com.example.store.model.ProductImage;
import com.example.store.repository.CategoryRepository;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.FavoriteRepository;
import com.example.store.repository.ProductImageRepository;
import com.example.store.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProductImageRepository productImageRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          CartItemRepository cartItemRepository, FavoriteRepository favoriteRepository,
                          ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.favoriteRepository = favoriteRepository;
        this.productImageRepository = productImageRepository;
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

    @Transactional
    public Product uploadProductImage(Integer id, MultipartFile file) {
        requireProduct(id);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An image file is required");
        }
        if (file.getSize() > 2L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image must not exceed 2 MB");
        }

        try {
            byte[] data = file.getBytes();
            String contentType = contentTypeFor(data);
            if (contentType == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPEG, PNG, and WebP images are allowed");
            }
            Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
            productImageRepository.save(id, contentType, data, updatedAt);
            return productRepository.setImageUrl(id, "/products/" + id + "/image?v=" + updatedAt.getTime());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read the uploaded image");
        }
    }

    @Transactional
    public Product deleteProductImage(Integer id) {
        requireProduct(id);
        productImageRepository.deleteByProductId(id);
        return productRepository.setImageUrl(id, null);
    }

    public ProductImage getPublicProductImage(Integer id) {
        if (productRepository.findPublicProductById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        ProductImage image = productImageRepository.findByProductId(id);
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product image not found");
        }
        return image;
    }

    private Product requireProduct(Integer id) {
        Product product = productRepository.findProductById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return product;
    }

    private String contentTypeFor(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (data.length >= 8 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47
                && data[4] == 0x0D && data[5] == 0x0A && data[6] == 0x1A && data[7] == 0x0A) {
            return "image/png";
        }
        if (data.length >= 12 && data[0] == 0x52 && data[1] == 0x49 && data[2] == 0x46 && data[3] == 0x46
                && data[8] == 0x57 && data[9] == 0x45 && data[10] == 0x42 && data[11] == 0x50) {
            return "image/webp";
        }
        return null;
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
