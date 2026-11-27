package io.student.rcc.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.exception.GlobalExceptionHandler;
import io.student.rcc.gateway.model.MuseumJson;
import io.student.rcc.gateway.service.MuseumClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MuseumControllerTest {

    @Mock
    private MuseumClient museumClient;

    private MockMvc mockMvc;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MuseumController(museumClient))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnMuseumsPageWithoutTitle() throws Exception {
        when(museumClient.getMuseumsPage(0, 20))
                .thenReturn(Map.of("content", List.of(), "totalPages", 0, "totalElements", 0L));

        mockMvc.perform(get("/api/museum"))
                .andExpect(status().isOk());
        verify(museumClient).getMuseumsPage(0, 20);
    }

    @Test
    void shouldReturnMuseumsPageWithTitle() throws Exception {
        when(museumClient.getMuseumsPage(eq("эрм"), eq(0), eq(20)))
                .thenReturn(Map.of("content", List.of(), "totalPages", 0, "totalElements", 0L));

        mockMvc.perform(get("/api/museum").param("title", "эрм"))
                .andExpect(status().isOk());
        verify(museumClient).getMuseumsPage("эрм", 0, 20);
    }

    @Test
    void shouldReturnAllMuseums() throws Exception {
        when(museumClient.getAllMuseums())
                .thenReturn(List.of(new MuseumJson("id", "Эрмитаж", null, null, null)));

        mockMvc.perform(get("/api/museum/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Эрмитаж"));
    }

    @Test
    void shouldReturnMuseumById() throws Exception {
        when(museumClient.getMuseumById("id"))
                .thenReturn(new MuseumJson("id", "Эрмитаж", null, null, null));

        mockMvc.perform(get("/api/museum/{id}", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Эрмитаж"));
    }

    @Test
    void shouldCreateMuseum() throws Exception {
        MuseumJson request = new MuseumJson(
                null, "Третьяковка", null, null,
                new MuseumJson.Geo("Москва", new MuseumJson.Country("c-1", null))
        );
        when(museumClient.createMuseum(any(MuseumJson.class)))
                .thenReturn(new MuseumJson("new", "Третьяковка", null, null, null));

        mockMvc.perform(post("/api/museum")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("new"));
    }

    @Test
    void shouldReturn400WhenTitleBlank() throws Exception {
        MuseumJson request = new MuseumJson(null, "", null, null, null);

        mockMvc.perform(post("/api/museum")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(museumClient);
    }

    @Test
    void shouldReturn404WhenGrpcNotFound() throws Exception {
        when(museumClient.getMuseumById("missing"))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/museum/{id}", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteMuseum() throws Exception {
        mockMvc.perform(delete("/api/museum/{id}", "id"))
                .andExpect(status().isNoContent());
        verify(museumClient).deleteMuseum("id");
    }
}