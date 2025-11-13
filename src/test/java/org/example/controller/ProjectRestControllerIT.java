package org.example.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты REST-контроллера проектов.
 * Поднимаем реальный Spring-контекст и дергаем HTTP-эндпоинты через MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(
        statements = "TRUNCATE TABLE projects RESTART IDENTITY CASCADE",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ProjectRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createProject_returns201_andBody() throws Exception {
        String body = """
                {"name":"AI Core","description":"Core services"}
                """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("AI Core"))
                .andExpect(jsonPath("$.description").value("Core services"))
                .andExpect(jsonPath("$.createdAt").exists())
                // убеждаемся, что время в ISO-формате с UTC-суффиксом Z
                .andExpect(jsonPath("$.createdAt", Matchers.endsWith("Z")));
    }

    @Test
    void listProjects_returns200_andArrayContainsCreated() throws Exception {
        // создаём проект
        String body = """
                {"name":"AI Core","description":"Core services"}
                """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // запрашиваем страницу
        mockMvc.perform(get("/api/projects")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // массив лежит в $.content
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").isNumber())
                .andExpect(jsonPath("$.content[0].name").value("AI Core"))
                .andExpect(jsonPath("$.content[0].description").value("Core services"))
                .andExpect(jsonPath("$.content[0].createdAt", Matchers.endsWith("Z")))
                // проверяем метаданные страницы
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.first").value(true));
    }

    @Test
    void createProject_withBlankName_returns400_andErrorJson() throws Exception {
        // name только из пробелов — должно сработать @NotBlank
        String body = """
                {"name":"   ","description":"x"}
                """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/projects"))
                .andExpect(jsonPath("$.message", Matchers.containsString("name must not be blank")));
    }

    @Test
    void get_notExisting_returns404_andErrorJson() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", Matchers.containsString("Project 999999 not found")));
    }

    @Test
    void delete_notExisting_returns404() throws Exception {
        mockMvc.perform(delete("/api/projects/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_notExisting_returns404() throws Exception {
        mockMvc.perform(put("/api/projects/{id}", 999_999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"X","description":"Y"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_conflict_sameName_returns409() throws Exception {
        // 1) создаём два проекта с разными именами
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alpha","description":"x"}
                                """))
                .andExpect(status().isCreated());

        var created2 = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Beta","description":"y"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // достаём id второго как Number → long
        long id2 = ((Number) com.jayway.jsonpath.JsonPath.read(created2, "$.id")).longValue();

        // 2) пробуем ПЕРЕИМЕНОВАТЬ второй в "Alpha" → конфликт 409
        mockMvc.perform(put("/api/projects/{id}", id2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alpha","description":"y2"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", Matchers.containsString("already exists")));
    }

    @Test
    void put_sameNameSameEntity_isOk() throws Exception {
        var created = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Gamma","description":"x"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = ((Number) com.jayway.jsonpath.JsonPath.read(created, "$.id")).longValue();

        // Обновляем тем же именем — это ОК (не конфликтуем сами с собой)
        mockMvc.perform(put("/api/projects/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Gamma","description":"x2"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Gamma"))
                .andExpect(jsonPath("$.description").value("x2"));
    }

    @Test
    void list_withNameFilter_returnsOnlyMatching() throws Exception {
        // создаём 3 проекта
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "AI Core",
                                  "description": "x"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Mainframe AI",
                                  "description": "y"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Beta",
                                  "description": "z"
                                }
                                """))
                .andExpect(status().isCreated());

        // фильтруем по name=ai (регистронезависимо)
        mockMvc.perform(get("/api/projects")
                        .param("name", "ai")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // вернулись только подходящие 2 записи
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("AI Core"))
                .andExpect(jsonPath("$.content[1].name").value("Mainframe AI"));
    }

    @Test
    void list_withBlankNameFilter_behavesLikeNoFilter() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .param("name", "   ") // только пробелы -> игнорируем
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}