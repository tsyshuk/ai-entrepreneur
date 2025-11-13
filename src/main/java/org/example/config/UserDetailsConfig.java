package org.example.config;

import org.example.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.*;

@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return email -> repo.findByEmailIgnoreCase(email)
                .map(u -> User
                        .withUsername(u.getEmail())
                        .password(u.getPasswordHash())     // уже BCrypt-хэш
                        .roles(u.getRole().name())         // ROLE_USER / ROLE_ADMIN
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}