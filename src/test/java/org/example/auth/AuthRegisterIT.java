package org.example.auth;

import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthRegisterIT {

    @Autowired MockMvc mvc;
    @Autowired UserRepository repo;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void cleanup() {
        // Убираем тестовых пользователей, если они остались от предыдущих запусков
        repo.findByEmailIgnoreCase("newuser@example.com")
                .ifPresent(u -> repo.deleteById(u.getId()));
        repo.findByEmailIgnoreCase("dup@example.com")
                .ifPresent(u -> repo.deleteById(u.getId()));
    }

    @Test
    void register_createsUser_withHashedPassword_and201() throws Exception {
        String json = """
            {"email":"newuser@example.com","password":"Str0ngPass!"}
        """;

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesRegex("/api/users/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.createdAt").exists());

        var u = repo.findByEmailIgnoreCase("newuser@example.com").orElseThrow();
        assertThat(u.getPasswordHash()).isNotEqualTo("Str0ngPass!");
        assertThat(encoder.matches("Str0ngPass!", u.getPasswordHash())).isTrue();
    }

    @Test
    void register_duplicateEmail_conflict409() throws Exception {
        // предварительно создаём пользователя с таким email
        var u = new User();
        u.setEmail("dup@example.com");
        u.setPasswordHash(encoder.encode("x"));
        u.setRole(UserRole.USER);
        repo.saveAndFlush(u);

        String json = """
            {"email":"dup@example.com","password":"AnyPass123"}
        """;

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }
}