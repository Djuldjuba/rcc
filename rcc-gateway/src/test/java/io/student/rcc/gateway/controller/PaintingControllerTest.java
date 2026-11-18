package io.student.rcc.gateway.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.exception.GlobalExceptionHandler;
import io.student.rcc.gateway.model.PaintingJson;
import io.student.rcc.gateway.service.PaintingClient;
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
class PaintingControllerTest {

    @Mock
    private PaintingClient paintingClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PaintingController(paintingClient))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnPaintingsPage() throws Exception {
        when(paintingClient.getPaintingsPage(0, 9))
                .thenReturn(Map.of("content", List.of(), "totalPages", 0, "totalElements", 0L));

        mockMvc.perform(get("/api/painting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(paintingClient).getPaintingsPage(0, 9);
    }

    @Test
    void shouldUseCustomPageAndSize() throws Exception {
        when(paintingClient.getPaintingsPage(1, 5))
                .thenReturn(Map.of("content", List.of(), "totalPages", 0, "totalElements", 0L));

        mockMvc.perform(get("/api/painting")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(paintingClient).getPaintingsPage(1, 5);
    }

    @Test
    void shouldReturnPaintingById() throws Exception {
        when(paintingClient.getPaintingById("p-1"))
                .thenReturn(new PaintingJson(
                        "p-1", "Мона Лиза", null, null,
                        new PaintingJson.ArtistJson("a-1", "Леонардо"),
                        new PaintingJson.MuseumJson("m-1", "Лувр")
                ));

        mockMvc.perform(get("/api/painting/{id}", "p-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"))
                .andExpect(jsonPath("$.title").value("Мона Лиза"))
                .andExpect(jsonPath("$.artist.name").value("Леонардо"))
                .andExpect(jsonPath("$.museum.title").value("Лувр"));
    }

    @Test
    void shouldReturn404WhenPaintingNotFound() throws Exception {
        when(paintingClient.getPaintingById("missing"))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/painting/{id}", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnPaintingsByArtist() throws Exception {
        when(paintingClient.getPaintingsByArtist("a-1", 0, 9))
                .thenReturn(Map.of("content", List.of(), "totalPages", 0, "totalElements", 0L));

        mockMvc.perform(get("/api/painting/author/{artistId}", "a-1"))
                .andExpect(status().isOk());

        verify(paintingClient).getPaintingsByArtist("a-1", 0, 9);
    }

    @Test
    void shouldReturnPaintingsByMuseum() throws Exception {
        when(paintingClient.getPaintingsByMuseum("m-1", 0, 9))
                .thenReturn(Map.of("content", List.of(), "totalPages", 0, "totalElements", 0L));

        mockMvc.perform(get("/api/painting/museum/{museumId}", "m-1"))
                .andExpect(status().isOk());

        verify(paintingClient).getPaintingsByMuseum("m-1", 0, 9);
    }

    @Test
    void shouldCreatePainting() throws Exception {
        when(paintingClient.addPainting(any(PaintingJson.class)))
                .thenReturn(new PaintingJson("new", "Т", null, null, null, null));

        String body = """
                {
                  "title": "Т",
                  "artist": { "id": "a-1" },
                  "museum": { "id": "m-1" }
                }
                """;

        mockMvc.perform(post("/api/painting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new"));

        verify(paintingClient).addPainting(any(PaintingJson.class));
    }

    @Test
    void shouldUpdatePainting() throws Exception {
        when(paintingClient.updatePainting(eq("p-1"), any(PaintingJson.class)))
                .thenReturn(new PaintingJson("p-1", "New", null, null, null, null));

        String body = """
                { "title": "New" }
                """;

        mockMvc.perform(put("/api/painting/{id}", "p-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"));
    }

    @Test
    void shouldDeletePainting() throws Exception {
        mockMvc.perform(delete("/api/painting/{id}", "p-1"))
                .andExpect(status().isOk());

        verify(paintingClient).deletePainting("p-1");
    }

    @Test
    void shouldReturn503WhenGrpcUnavailable() throws Exception {
        when(paintingClient.getPaintingById("p-1"))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        mockMvc.perform(get("/api/painting/{id}", "p-1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void shouldReturn409WhenGrpcAlreadyExists() throws Exception {
        when(paintingClient.addPainting(any(PaintingJson.class)))
                .thenThrow(new StatusRuntimeException(Status.ALREADY_EXISTS));

        mockMvc.perform(post("/api/painting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T\"}"))
                .andExpect(status().isConflict());
    }
}