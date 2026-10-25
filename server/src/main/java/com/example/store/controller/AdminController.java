package com.example.store.controller;

import com.example.store.model.CustomUser;
import com.example.store.model.ApiErrorResponse;
import com.example.store.model.Order;
import com.example.store.model.OrderDetails;
import com.example.store.model.OrderStatusRequest;
import com.example.store.model.Product;
import com.example.store.model.RoleUpdateRequest;
import com.example.store.service.OrderService;
import com.example.store.service.ProductService;
import com.example.store.service.UserService;
import com.example.store.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public AdminController(UserService userService, ProductService productService, OrderService orderService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    private String extractUsername(String token) {
        return jwtUtil.extractUsername(token.substring(7));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all-users")
    public ResponseEntity<List<CustomUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete-user/{username}")
    public ResponseEntity<?> deleteAnotherUser(@RequestHeader("Authorization") String token, @PathVariable String username) {
        if (extractUsername(token).equals(username)) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("An admin cannot delete their own account"));
        }
        String result = userService.deleteUser(username);
        return result.contains("successfully") ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(new ApiErrorResponse(result));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/users/{username}/role")
    public ResponseEntity<?> updateUserRole(@RequestHeader("Authorization") String token, @PathVariable String username,
                                             @RequestBody RoleUpdateRequest request) {
        if (extractUsername(token).equals(username)) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("An admin cannot change their own role"));
        }
        if (request == null || request.getRole() == null) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("Role must be USER or ADMIN"));
        }
        CustomUser user = userService.updateUserRole(username, request.getRole());
        return user == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("User not found")) : ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() { return ResponseEntity.ok(productService.getAllProducts()); }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(product));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Product> deactivateProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.deactivateProduct(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/products/{id}/restore")
    public ResponseEntity<Product> restoreProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.restoreProduct(id));
    }

    @PutMapping(value = "/products/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> uploadProductImage(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(productService.uploadProductImage(id, file));
    }

    @DeleteMapping("/products/{id}/image")
    public ResponseEntity<Product> deleteProductImage(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.deleteProductImage(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getOrders() { return ResponseEntity.ok(orderService.getAllOrders()); }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDetails> getOrder(@PathVariable Integer id) { return ResponseEntity.ok(orderService.getAdminOrder(id)); }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Integer id, @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }
}
