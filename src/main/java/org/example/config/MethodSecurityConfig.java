package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // включает @PreAuthorize / @PostAuthorize
public class MethodSecurityConfig {
}