// src/test/java/org/example/security/ProjectsRoleIT.java
package org.example.security;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectsRoleIT {

    @Autowired MockMvc mvc;
    @Autowired UserRepository repo;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void prepareUsers() {
        repo.findByEmailIgnoreCase("user@example.com").ifPresent(u -> repo.deleteById(u.getId()));
        repo.findByEmailIgnoreCase("admin@example.com").ifPresent(u -> repo.deleteById(u.getId()));

        var user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash(encoder.encode("Qwerty_123"));
        user.setRole(UserRole.USER);
        repo.saveAndFlush(user);

        var admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(encoder.encode("Admin_123"));
        admin.setRole(UserRole.ADMIN);
        repo.saveAndFlush(admin);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String json = """
          {"email":"%s","password":"%s"}
        """.formatted(email, password);

        var resp = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(resp).get("accessToken").asText();
    }

    @Test
    void projects_withoutToken_401() throws Exception {
        mvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void projects_withAdminToken_403() throws Exception {
        String token = loginAndGetToken("admin@example.com", "Admin_123");
        mvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void projects_withUserToken_200() throws Exception {
        String token = loginAndGetToken("user@example.com", "Qwerty_123");
        mvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}