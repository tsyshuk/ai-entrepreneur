package org.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class JwtFilterConfig {
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwt, UserDetailsService uds) {
        return new JwtAuthenticationFilter(jwt, uds);
    }
}