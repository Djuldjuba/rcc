package io.student.rcc.gateway.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.exception.GlobalExceptionHandler;
import io.student.rcc.gateway.model.CountryJson;
import io.student.rcc.gateway.service.CountryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CountryControllerTest {

    @Mock
    private CountryClient countryClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CountryController(countryClient))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnCountriesPage() throws Exception {
        when(countryClient.getCountriesPage(0, 20, null))
                .thenReturn(Map.of(
                        "content", List.of(),
                        "totalPages", 0,
                        "totalElements", 0L,
                        "page", 0,
                        "size", 20
                ));

        mockMvc.perform(get("/api/country"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(countryClient).getCountriesPage(0, 20, null);
    }

    @Test
    void shouldPassTitleFilter() throws Exception {
        when(countryClient.getCountriesPage(eq(0), eq(20), eq("Рос")))
                .thenReturn(Map.of(
                        "content", List.of(),
                        "totalPages", 0,
                        "totalElements", 0L,
                        "page", 0,
                        "size", 20
                ));

        mockMvc.perform(get("/api/country").param("title", "Рос"))
                .andExpect(status().isOk());

        verify(countryClient).getCountriesPage(0, 20, "Рос");
    }

    @Test
    void shouldUseCustomPagination() throws Exception {
        when(countryClient.getCountriesPage(eq(2), eq(5), any()))
                .thenReturn(Map.of(
                        "content", List.of(),
                        "totalPages", 3,
                        "totalElements", 12L,
                        "page", 2,
                        "size", 5
                ));

        mockMvc.perform(get("/api/country")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.totalElements").value(12));
    }

    @Test
    void shouldReturnCountryById() throws Exception {
        when(countryClient.getCountryById("c-1"))
                .thenReturn(new CountryJson("c-1", "Россия"));

        mockMvc.perform(get("/api/country/{id}", "c-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c-1"))
                .andExpect(jsonPath("$.name").value("Россия"));
    }

    @Test
    void shouldReturn404WhenCountryNotFound() throws Exception {
        when(countryClient.getCountryById("missing"))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/country/{id}", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnCountryByName() throws Exception {
        when(countryClient.getCountryByName("Россия"))
                .thenReturn(new CountryJson("c-1", "Россия"));

        mockMvc.perform(get("/api/country/name/{name}", "Россия"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c-1"))
                .andExpect(jsonPath("$.name").value("Россия"));
    }

    @Test
    void shouldReturn404WhenCountryByNameNotFound() throws Exception {
        when(countryClient.getCountryByName("Атлантида"))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/country/name/{name}", "Атлантида"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn503WhenGrpcUnavailable() throws Exception {
        when(countryClient.getCountryById("c-1"))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        mockMvc.perform(get("/api/country/{id}", "c-1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void shouldReturn400WhenGrpcInvalidArgument() throws Exception {
        when(countryClient.getCountryById("bad"))
                .thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

        mockMvc.perform(get("/api/country/{id}", "bad"))
                .andExpect(status().isBadRequest());
    }
}