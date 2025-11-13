package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // ко всем эндпоинтам
                .allowedOrigins("http://localhost:3000") // разрешаем фронту
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // методы
                .allowedHeaders("*") // разрешённые заголовки (в т.ч. Authorization)
                // .allowCredentials(true)
                .maxAge(3600); // кешировать preflight а 1 час
    }
}
