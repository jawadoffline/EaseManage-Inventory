package com.easemanage.config;

import com.easemanage.user.entity.Role;
import com.easemanage.user.entity.Status;
import com.easemanage.user.entity.User;
import com.easemanage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        var existing = userRepository.findByUsername("admin");
        if (existing.isEmpty()) {
            User admin = User.builder()
                .username("admin")
                .email("admin@easemanage.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .firstName("System")
                .lastName("Admin")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();
            userRepository.save(admin);
        } else {
            // Fix password hash if it doesn't match (e.g. from V1 migration seed)
            User admin = existing.get();
            if (!passwordEncoder.matches("admin123", admin.getPasswordHash())) {
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                userRepository.save(admin);
            }
        }
    }
}
