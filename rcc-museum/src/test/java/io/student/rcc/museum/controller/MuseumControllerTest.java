package io.student.rcc.museum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.student.rcc.museum.domain.CreateMuseumRequest;
import io.student.rcc.museum.domain.UpdateMuseumRequest;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MuseumControllerTest {

    private static final String FIXTURE =
            "/io/student/rcc/museum/controller/MuseumControllerTest.sql";

    private static final String ID_HERMITAGE = "11111111-1111-1111-1111-111111111111";
    private static final String COUNTRY_RU = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper om = new ObjectMapper();

    @Test
    @Sql(FIXTURE)
    void shouldReturnMuseumById() throws Exception {
        mockMvc.perform(get("/api/museum/{id}", ID_HERMITAGE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_HERMITAGE))
                .andExpect(jsonPath("$.title").value("Эрмитаж"))
                .andExpect(jsonPath("$.city").value("Санкт-Петербург"));
    }

    @Test
    void shouldReturn404WhenMuseumNotFoundById() throws Exception {
        mockMvc.perform(get("/api/museum/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturnAllMuseums() throws Exception {
        mockMvc.perform(get("/api/museum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title").value("Эрмитаж"));
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturnMuseumsPage() throws Exception {
        mockMvc.perform(get("/api/museum/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @Sql(FIXTURE)
    void shouldFilterMuseumsByTitle() throws Exception {
        mockMvc.perform(get("/api/museum/page")
                        .param("title", "эрми")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Эрмитаж"));
    }

    @Test
    void shouldReturnEmptyPageWhenNoMuseums() throws Exception {
        mockMvc.perform(get("/api/museum/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturnMuseumsByCountry() throws Exception {
        mockMvc.perform(get("/api/museum/country/{countryId}", COUNTRY_RU)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Эрмитаж"));
    }

    @Test
    void shouldCreateMuseum() throws Exception {
        CreateMuseumRequest.GeoWrapper geo = new CreateMuseumRequest.GeoWrapper(
                "Москва",
                new CreateMuseumRequest.CountryRef(COUNTRY_RU)
        );

        CreateMuseumRequest request = new CreateMuseumRequest(
                "Третьяковская галерея",
                "Музей русского искусства",
                null,
                geo
        );

        mockMvc.perform(post("/api/museum")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Третьяковская галерея"))
                .andExpect(jsonPath("$.city").value("Москва"))
                .andExpect(jsonPath("$.id").value(Matchers.matchesPattern(
                        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
                )));
    }

    @Test
    void shouldReturn401WhenCreateWithoutAuth() throws Exception {
        CreateMuseumRequest.GeoWrapper geo = new CreateMuseumRequest.GeoWrapper(
                "Москва",
                new CreateMuseumRequest.CountryRef(COUNTRY_RU)
        );

        CreateMuseumRequest request = new CreateMuseumRequest(
                "Третьяковка",
                "Описание",
                null,
                geo
        );

        mockMvc.perform(post("/api/museum")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Sql(FIXTURE)
    void shouldUpdateMuseum() throws Exception {
        UpdateMuseumRequest.GeoWrapper geo = new UpdateMuseumRequest.GeoWrapper(
                "СПб",
                new UpdateMuseumRequest.CountryRef(COUNTRY_RU)
        );

        UpdateMuseumRequest request = new UpdateMuseumRequest(
                "Эрмитаж (обновлён)",
                "Новое описание",
                null,
                geo
        );

        mockMvc.perform(put("/api/museum/{id}", ID_HERMITAGE)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Эрмитаж (обновлён)"))
                .andExpect(jsonPath("$.city").value("СПб"));
    }

    @Test
    @Sql(FIXTURE)
    void shouldDeleteMuseum() throws Exception {
        mockMvc.perform(delete("/api/museum/{id}", ID_HERMITAGE)
                        .with(jwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/museum/{id}", ID_HERMITAGE)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(FIXTURE)
    void getShouldBePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/museum/{id}", ID_HERMITAGE))
                .andExpect(status().isOk());
    }
}