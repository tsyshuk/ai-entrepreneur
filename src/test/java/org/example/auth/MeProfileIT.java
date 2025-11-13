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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class MeProfileIT {

    @Autowired MockMvc mvc;
    @Autowired UserRepository repo;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        repo.findByEmailIgnoreCase("me@example.com").ifPresent(u -> repo.deleteById(u.getId()));
        var u = new User();
        u.setEmail("me@example.com");
        u.setPasswordHash(encoder.encode("Qwerty_123"));
        u.setRole(UserRole.USER);
        repo.saveAndFlush(u);
    }

    private String loginAndGetToken() throws Exception {
        String json = """
          {"email":"me@example.com","password":"Qwerty_123"}
        """;

        var resp = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return new ObjectMapper().readTree(resp).get("accessToken").asText();
    }

    @Test
    void me_withoutToken_401() throws Exception {
        mvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withValidToken_returnsProfile() throws Exception {
        String token = loginAndGetToken();

        mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.id").isNumber());
    }
}