package io.student.rcc.painting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.student.rcc.painting.domain.CreatePaintingRequest;
import io.student.rcc.painting.domain.UpdatePaintingRequest;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaintingControllerTest {

    private static final String FIXTURE = "/io/student/rcc/painting/controller/PaintingControllerTest.sql";

    private static final Matcher<String> idMatcher = Matchers.matchesPattern(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper om = new ObjectMapper();

    @Test
    @Sql(FIXTURE)
    void shouldReturnPaintingById() throws Exception {
        String fixtureId = "11111111-1111-1111-1111-111111111111";

        mockMvc.perform(get("/api/painting/{id}", fixtureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fixtureId))
                .andExpect(jsonPath("$.title").value("Над вечным покоем"))
                .andExpect(jsonPath("$.description").value("Пейзаж Левитана"))
                .andExpect(jsonPath("$.artistId").value(idMatcher));
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturnPaintingsPage() throws Exception {
        mockMvc.perform(get("/api/painting/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Над вечным покоем"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturnPaintingsByArtist() throws Exception {
        String artistId = "22222222-2222-2222-2222-222222222222";

        mockMvc.perform(get("/api/painting/artist/{artistId}", artistId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Над вечным покоем"));
    }

    @Test
    void shouldReturn404WhenPaintingNotFound() throws Exception {
        String unknownId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/painting/{id}", unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldCreatePainting() throws Exception {
        CreatePaintingRequest request = new CreatePaintingRequest(
                "Девятый вал",
                "Картина Айвазовского",
                "data:image/jpeg;base64,abc",
                "22222222-2222-2222-2222-222222222222",
                "33333333-3333-3333-3333-333333333333"
        );

        mockMvc.perform(post("/api/painting")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(idMatcher))
                .andExpect(jsonPath("$.title").value("Девятый вал"))
                .andExpect(jsonPath("$.artistId").value("22222222-2222-2222-2222-222222222222"));
    }

    @Test
    @Sql(FIXTURE)
    @WithMockUser
    void shouldUpdatePainting() throws Exception {
        String fixtureId = "11111111-1111-1111-1111-111111111111";

        UpdatePaintingRequest request = new UpdatePaintingRequest(
                "Обновлённое название",
                "Новое описание",
                "data:image/jpeg;base64,new",
                "22222222-2222-2222-2222-222222222222",
                "33333333-3333-3333-3333-333333333333"
        );

        mockMvc.perform(put("/api/painting/{id}", fixtureId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fixtureId))
                .andExpect(jsonPath("$.title").value("Обновлённое название"));
    }

    @Test
    @Sql(FIXTURE)
    @WithMockUser
    void shouldDeletePainting() throws Exception {
        String fixtureId = "11111111-1111-1111-1111-111111111111";

        mockMvc.perform(delete("/api/painting/{id}", fixtureId)
                        .with(jwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/painting/{id}", fixtureId)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }
}