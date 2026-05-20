package com.authService.config;

import com.authService.entity.Role;
import com.authService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ================================================================
 * Data Initializer - Application startup par roles seed karta hai
 * ================================================================
 * CommandLineRunner: Spring Boot fully start hone ke baad run hota hai.
 *
 * Pehli baar application start hone par roles table mein
 * ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN insert hote hain.
 * Agar already hain to skip karo (idempotent).
 *
 * Yeh AuthService.register() mein roleRepository.findByName(ROLE_USER)
 * ke liye zaruri hai - warna "Default role not found" error aayega.
 * ================================================================
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Checking and initializing default roles...");

        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            } else {
                log.debug("Role already exists: {}", roleName);
            }
        }

        log.info("Role initialization complete. Total roles: {}", roleRepository.count());
    }
}
