package io.student.rcc.artist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.student.rcc.artist.domain.CreateArtistRequest;
import io.student.rcc.artist.domain.UpdateArtistRequest;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ArtistControllerTest {

    private static final String FIXTURE =
            "/io/student/rcc/artist/controller/ArtistControllerTest.sql";

    private static final String FIXTURE_ARTIST_ID = "11111111-1111-1111-1111-111111111111";

    private static final Matcher<String> idMatcher = Matchers.matchesPattern(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper om = new ObjectMapper();

    @Test
    @Sql(FIXTURE)
    void shouldReturnAllArtists() throws Exception {
        mockMvc.perform(get("/api/artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", hasItem("Иван Айвазовский")))
                .andExpect(jsonPath("$[*].name", hasItem("Исаак Левитан")))
                .andExpect(jsonPath("$[0].id").value(idMatcher));
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturnArtistById() throws Exception {
        mockMvc.perform(get("/api/artist/{id}", FIXTURE_ARTIST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FIXTURE_ARTIST_ID))
                .andExpect(jsonPath("$.name").value("Иван Айвазовский"))
                .andExpect(jsonPath("$.biography").value("Русский художник-маринист"));
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturnArtistsPage() throws Exception {
        mockMvc.perform(get("/api/artist/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldReturn404WhenArtistNotFound() throws Exception {
        mockMvc.perform(get("/api/artist/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateArtist() throws Exception {
        CreateArtistRequest request = new CreateArtistRequest(
                "Василий Кандинский",
                "Русский художник-абстракционист",
                "data:image/jpeg;base64,abc"
        );

        mockMvc.perform(post("/api/artist")
                        .with(jwt().jwt(j -> j.subject("test-user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(idMatcher))
                .andExpect(jsonPath("$.name").value("Василий Кандинский"))
                .andExpect(jsonPath("$.biography").value("Русский художник-абстракционист"));
    }

    @Test
    void shouldReturn400WhenCreateArtistWithBlankName() throws Exception {
        CreateArtistRequest request = new CreateArtistRequest("", "Биография", null);

        mockMvc.perform(post("/api/artist")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturn400WhenCreateArtistWithDuplicateName() throws Exception {
        CreateArtistRequest request = new CreateArtistRequest(
                "Иван Айвазовский",
                "Дубликат",
                null
        );

        mockMvc.perform(post("/api/artist")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenCreateArtistWithoutJwt() throws Exception {
        CreateArtistRequest request = new CreateArtistRequest("Без токена", "Биография", null);

        mockMvc.perform(post("/api/artist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Sql(FIXTURE)
    void shouldUpdateArtist() throws Exception {
        UpdateArtistRequest request = new UpdateArtistRequest(
                "Иван Айвазовский (обновлён)",
                "Новая биография",
                "data:image/jpeg;base64,new"
        );

        mockMvc.perform(put("/api/artist/{id}", FIXTURE_ARTIST_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FIXTURE_ARTIST_ID))
                .andExpect(jsonPath("$.name").value("Иван Айвазовский (обновлён)"))
                .andExpect(jsonPath("$.biography").value("Новая биография"));
    }

    @Test
    void shouldReturn404WhenUpdateNotFound() throws Exception {
        UpdateArtistRequest request = new UpdateArtistRequest("Кто-то", "Биография", null);

        mockMvc.perform(put("/api/artist/{id}", UUID.randomUUID())
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(FIXTURE)
    void shouldDeleteArtist() throws Exception {
        mockMvc.perform(delete("/api/artist/{id}", FIXTURE_ARTIST_ID).with(jwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/artist/{id}", FIXTURE_ARTIST_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeleteNotFound() throws Exception {
        mockMvc.perform(delete("/api/artist/{id}", UUID.randomUUID()).with(jwt()))
                .andExpect(status().isNotFound());
    }
}