package com.example.store;

import com.example.store.model.CustomUser;
import com.example.store.model.Product;
import com.example.store.model.Role;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.UserRepository;
import com.example.store.security.CustomUserDetailsService;
import com.example.store.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ProductImageIntegrationTests {
    private static final String ADMIN = "product_image_admin_test";
    private static final String USER = "product_image_user_test";
    private static final int PRODUCT_ID = 1;
    private static final byte[] PNG_BYTES = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00};

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private FilterChainProxy springSecurityFilterChain;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomUserDetailsService customUserDetailsService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;
    private String originalImageUrl;

    @BeforeEach
    void prepareData() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain).build();
        Product product = productRepository.findProductById(PRODUCT_ID);
        originalImageUrl = product.getImageUrl();
        jdbcTemplate.update("DELETE FROM product_images WHERE product_id = ?", PRODUCT_ID);
        jdbcTemplate.update("DELETE FROM users WHERE username IN (?, ?)", ADMIN, USER);
        registerUser(ADMIN, Role.ADMIN);
        registerUser(USER, Role.USER);
    }

    @AfterEach
    void cleanData() {
        jdbcTemplate.update("DELETE FROM product_images WHERE product_id = ?", PRODUCT_ID);
        jdbcTemplate.update("UPDATE products SET image_url = ? WHERE id = ?", originalImageUrl, PRODUCT_ID);
        jdbcTemplate.update("DELETE FROM users WHERE username IN (?, ?)", ADMIN, USER);
    }

    @Test
    void userTokenIsForbiddenFromImageUploadAndDelete() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/admin/products/{id}/image", PRODUCT_ID)
                        .file(imageFile(PNG_BYTES)).header("Authorization", bearerToken(USER)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/admin/products/{id}/image", PRODUCT_ID).header("Authorization", bearerToken(USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidAndOversizedFilesReturnClearErrors() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/admin/products/{id}/image", PRODUCT_ID)
                        .file(new MockMultipartFile("file", "not-an-image.jpg", "image/jpeg", "not an image".getBytes()))
                        .header("Authorization", bearerToken(ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only JPEG, PNG, and WebP images are allowed"));

        byte[] oversized = new byte[(2 * 1024 * 1024) + 1];
        System.arraycopy(PNG_BYTES, 0, oversized, 0, PNG_BYTES.length);
        mockMvc.perform(multipart(HttpMethod.PUT, "/admin/products/{id}/image", PRODUCT_ID)
                        .file(imageFile(oversized)).header("Authorization", bearerToken(ADMIN)))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.message").value("Image must not exceed 2 MB"));
    }

    @Test
    void uploadServesStoredBytesAndUpdatesVersionedProductUrlWithoutAddingBytesToCatalog() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/admin/products/{id}/image", PRODUCT_ID)
                        .file(imageFile(PNG_BYTES)).header("Authorization", bearerToken(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", startsWith("/products/1/image?v=")));

        mockMvc.perform(get("/products/{id}/image", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(PNG_BYTES))
                .andExpect(header().string("Cache-Control", containsString("max-age=31536000")));
        mockMvc.perform(get("/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", startsWith("/products/1/image?v=")));
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].data").doesNotExist());
    }

    @Test
    void deleteRemovesStoredImageAndClearsProductUrl() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/admin/products/{id}/image", PRODUCT_ID)
                .file(imageFile(PNG_BYTES)).header("Authorization", bearerToken(ADMIN))).andExpect(status().isOk());

        mockMvc.perform(delete("/admin/products/{id}/image", PRODUCT_ID).header("Authorization", bearerToken(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.nullValue()));
        mockMvc.perform(get("/products/{id}/image", PRODUCT_ID)).andExpect(status().isNotFound());
        mockMvc.perform(get("/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.nullValue()));
    }

    private MockMultipartFile imageFile(byte[] data) {
        return new MockMultipartFile("file", "product.png", "image/png", data);
    }

    private void registerUser(String username, Role role) {
        userRepository.register(new CustomUser("Image", "Test", username + "@example.test", "0500000000",
                "Test Address", username, "not-used-in-this-test", role));
    }

    private String bearerToken(String username) {
        return "Bearer " + jwtUtil.generateToken(customUserDetailsService.loadUserByUsername(username));
    }
}
