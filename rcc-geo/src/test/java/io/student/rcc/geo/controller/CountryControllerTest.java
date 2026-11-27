package io.student.rcc.geo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CountryControllerTest {

    private static final String FIXTURE =
            "/io/student/rcc/geo/controller/CountryControllerTest.sql";

    private static final Matcher<String> idMatcher = Matchers.matchesPattern(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper om = new ObjectMapper();


    @Test
    @Sql(FIXTURE)
    void shouldReturnCountryById() throws Exception {
        String fixtureId = "11111111-1111-1111-1111-111111111111";

        mockMvc.perform(get("/api/country/{id}", fixtureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fixtureId))
                .andExpect(jsonPath("$.name").value("Россия"));
    }

    @Test
    void shouldReturn404WhenCountryNotFoundById() throws Exception {
        String unknownId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/country/{id}", unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturnCountryByName() throws Exception {
        mockMvc.perform(get("/api/country/name/{name}", "Япония"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idMatcher))
                .andExpect(jsonPath("$.name").value("Япония"));
    }

    @Test
    void shouldReturn404WhenCountryNotFoundByName() throws Exception {
        mockMvc.perform(get("/api/country/name/{name}", "Атлантида"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturnCountriesPage() throws Exception {
        mockMvc.perform(get("/api/country")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name").value("Россия"))
                .andExpect(jsonPath("$.content[1].name").value("Япония"))
                .andExpect(jsonPath("$.content[2].name").value("Германия"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @Sql(FIXTURE)
    void shouldRespectPageSize() throws Exception {
        mockMvc.perform(get("/api/country")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void shouldReturnEmptyPageWhenNoCountries() throws Exception {
        mockMvc.perform(get("/api/country")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @Sql(FIXTURE)
    void getShouldBePubliclyAccessible() throws Exception {
        String fixtureId = "11111111-1111-1111-1111-111111111111";

        mockMvc.perform(get("/api/country/{id}", fixtureId))
                .andExpect(status().isOk());
    }
}