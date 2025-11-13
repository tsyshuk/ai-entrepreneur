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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthLoginIT {

    @Autowired MockMvc mvc;
    @Autowired UserRepository repo;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void ensureUserExists() {
        repo.findByEmailIgnoreCase("me@example.com")
                .ifPresent(u -> repo.deleteById(u.getId()));

        var u = new User();
        u.setEmail("me@example.com");
        u.setPasswordHash(encoder.encode("Qwerty_123"));
        u.setRole(UserRole.USER);
        repo.saveAndFlush(u);
    }

    @Test
    void login_ok_returnsToken() throws Exception {
        String json = """
          {"email":"me@example.com","password":"Qwerty_123"}
        """;

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void login_wrongPassword_401() throws Exception {
        String json = """
          {"email":"me@example.com","password":"WRONG"}
        """;

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}