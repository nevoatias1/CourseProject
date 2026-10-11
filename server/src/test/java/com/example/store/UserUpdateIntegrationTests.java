package com.example.store;

import com.example.store.model.AuthenticationRequest;
import com.example.store.model.CustomUser;
import com.example.store.model.Role;
import com.example.store.repository.UserRepository;
import com.example.store.security.CustomUserDetailsService;
import com.example.store.service.AuthenticationService;
import com.example.store.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserUpdateIntegrationTests {
    private static final String USERNAME = "user_update_test";
    private static final String PASSWORD = "UserUpdatePass123!";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private FilterChainProxy springSecurityFilterChain;
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepareUser() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
        jdbcTemplate.update("DELETE FROM users WHERE username LIKE 'user_update_test%'");
        userRepository.register(new CustomUser("User", "Update", "user_update_test@example.test", "0500000000",
                "Original Address", USERNAME, passwordEncoder.encode(PASSWORD), Role.USER));
    }

    @Test
    void roleSubmittedAsAdminStaysUserAndAdminEndpointRemainsForbidden() throws Exception {
        mockMvc.perform(put("/users")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"first_name\":\"User\",\"last_name\":\"Update\",\"email\":\"user_update_test@example.test\",\"phone\":\"0500000000\",\"address\":\"New Address\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());

        assertEquals(Role.USER, userRepository.findUserByUsername(USERNAME).getRole());
        mockMvc.perform(get("/admin/all-users").header("Authorization", bearerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void submittedUsernameCannotReplaceJwtSubjectUsername() throws Exception {
        mockMvc.perform(put("/users")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"first_name\":\"User\",\"last_name\":\"Update\",\"email\":\"user_update_test@example.test\",\"phone\":\"0500000000\",\"address\":\"New Address\",\"username\":\"changed_username\"}"))
                .andExpect(status().isOk());

        assertNotNull(userRepository.findUserByUsername(USERNAME));
        assertNull(userRepository.findUserByUsername("changed_username"));
    }

    @Test
    void missingPasswordKeepsExistingPasswordUsableForAuthentication() throws Exception {
        mockMvc.perform(put("/users")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"first_name\":\"Updated\",\"last_name\":\"User\",\"email\":\"user_update_test@example.test\",\"phone\":\"0500000000\",\"address\":\"New Address\"}"))
                .andExpect(status().isOk());

        assertNotNull(authenticationService.createAuthenticationToken(new AuthenticationRequest(USERNAME, PASSWORD)).getJwt());
    }

    @Test
    void duplicateEmailReturnsConflictInsteadOfServerError() throws Exception {
        userRepository.register(new CustomUser("Other", "User", "other_user_update_test@example.test", "0500000001",
                "Other Address", "user_update_test_other", passwordEncoder.encode(PASSWORD), Role.USER));

        mockMvc.perform(put("/users")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"first_name\":\"User\",\"last_name\":\"Update\",\"email\":\"other_user_update_test@example.test\",\"phone\":\"0500000000\",\"address\":\"New Address\"}"))
                .andExpect(status().isConflict());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtil.generateToken(customUserDetailsService.loadUserByUsername(USERNAME));
    }
}
