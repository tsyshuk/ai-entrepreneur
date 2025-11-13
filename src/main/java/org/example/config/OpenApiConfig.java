package org.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Entrepreneur API")
                        .description("Учебный проект: проекты, пользователи, CRUD + валидация")
                        .version("v1")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("you@example.com")));
    }

    @Bean
    public GroupedOpenApi projectsGroup() {
        return GroupedOpenApi.builder()
                .group("projects")
                .pathsToMatch("/api/projects/**")
                .build();
    }

    @Bean
    public GroupedOpenApi usersGroup() {
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/users/**")
                .build();
    }
}