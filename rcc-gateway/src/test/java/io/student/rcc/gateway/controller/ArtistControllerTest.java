package io.student.rcc.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.exception.GlobalExceptionHandler;
import io.student.rcc.gateway.model.ArtistJson;
import io.student.rcc.gateway.service.ArtistClient;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ArtistControllerTest {

    @Mock
    private ArtistClient artistClient;

    private MockMvc mockMvc;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ArtistController(artistClient))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnArtistsPage() throws Exception {
        when(artistClient.getArtistsPage(eq("pic"), eq(0), eq(18)))
                .thenReturn(Map.of("content", List.of(), "totalPages", 0, "totalElements", 0L));

        mockMvc.perform(get("/api/artist").param("name", "pic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldReturnAllArtists() throws Exception {
        when(artistClient.getAllArtists())
                .thenReturn(List.of(new ArtistJson("id", "Picasso", null, null)));

        mockMvc.perform(get("/api/artist/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Picasso"));
    }

    @Test
    void shouldReturnArtistById() throws Exception {
        when(artistClient.getArtistById("id"))
                .thenReturn(new ArtistJson("id", "Picasso", null, null));

        mockMvc.perform(get("/api/artist/{id}", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Picasso"));
    }

    @Test
    void shouldCreateArtist() throws Exception {
        ArtistJson request = new ArtistJson(null, "Dali", null, null);
        when(artistClient.createArtist(any(ArtistJson.class)))
                .thenReturn(new ArtistJson("new", "Dali", null, null));

        mockMvc.perform(post("/api/artist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("new"));
    }

    @Test
    void shouldReturn400WhenNameBlank() throws Exception {
        ArtistJson request = new ArtistJson(null, "", null, null);

        mockMvc.perform(post("/api/artist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(artistClient);
    }

    @Test
    void shouldReturn404WhenGrpcNotFound() throws Exception {
        when(artistClient.getArtistById("missing"))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/artist/{id}", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateArtist() throws Exception {
        ArtistJson request = new ArtistJson(null, "New", null, null);
        when(artistClient.updateArtist(eq("id"), any(ArtistJson.class)))
                .thenReturn(new ArtistJson("id", "New", null, null));

        mockMvc.perform(put("/api/artist/{id}", "id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void shouldDeleteArtist() throws Exception {
        mockMvc.perform(delete("/api/artist/{id}", "id"))
                .andExpect(status().isNoContent());
        verify(artistClient).deleteArtist("id");
    }
}