// src/main/java/org/example/config/InitAdmin.java
package org.example.config;

import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitAdmin {

    @Bean
    CommandLineRunner createAdminUser(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            String email = "admin@example.com";
            String rawPassword = "Admin_123"; // тот самый из примера

            // если уже есть — ничего не делаем
            if (repo.existsByEmailIgnoreCase(email)) {
                return;
            }

            User admin = new User();
            admin.setEmail(email);
            admin.setPasswordHash(encoder.encode(rawPassword)); // ВАЖНО: шифруем!
            admin.setRole(UserRole.ADMIN);

            repo.saveAndFlush(admin);
        };
    }
}
