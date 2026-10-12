package com.example.store.security;

import com.example.store.model.CustomUser;
import com.example.store.model.Role;
import com.example.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionAdminInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public ProductionAdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUsername.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException("ADMIN_USERNAME and ADMIN_PASSWORD are required in the prod profile");
        }
        if (userRepository.findUserByUsername(adminUsername) != null) {
            return;
        }

        CustomUser admin = new CustomUser(
                "Store", "Administrator", adminUsername + "@admin.local", null, null,
                adminUsername, passwordEncoder.encode(adminPassword), Role.ADMIN
        );
        userRepository.register(admin);
    }
}
