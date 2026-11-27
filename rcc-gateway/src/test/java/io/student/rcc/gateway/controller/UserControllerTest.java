package io.student.rcc.gateway.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.model.UserJson;
import io.student.rcc.gateway.service.UserClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;

    @Test
    void shouldReturnCurrentUser() throws Exception {
        when(userClient.getCurrentUser())
                .thenReturn(new UserJson("u-1", "alice", "Alice", "Ivanova", null));

        mockMvc.perform(get("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject("alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("u-1"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.firstname").value("Alice"));

        verify(userClient).getCurrentUser();
    }

    @Test
    void shouldReturn401WhenGetWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userClient);
    }

    @Test
    void shouldReturn404WhenGrpcUserNotFound() throws Exception {
        when(userClient.getCurrentUser())
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject("ghost"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        when(userClient.updateUser(any(UserJson.class)))
                .thenReturn(new UserJson("u-1", "alice", "New", "Petrova", null));

        String body = """
                { "username": "alice", "firstname": "New", "lastname": "Petrova" }
                """;

        mockMvc.perform(patch("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject("alice")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("New"))
                .andExpect(jsonPath("$.lastname").value("Petrova"));
    }

    @Test
    void shouldReturn403WhenUsernameDiffersFromPrincipal() throws Exception {
        String body = """
                { "username": "bob", "firstname": "New" }
                """;

        mockMvc.perform(patch("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject("alice")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userClient);
    }

    @Test
    void shouldReturn401WhenPatchWithoutAuth() throws Exception {
        String body = """
                { "username": "alice" }
                """;

        mockMvc.perform(patch("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userClient);
    }

    @Test
    void shouldReturn400WhenGrpcInvalidArgument() throws Exception {
        when(userClient.updateUser(any(UserJson.class)))
                .thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

        String body = """
                { "username": "alice", "firstname": "X" }
                """;

        mockMvc.perform(patch("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject("alice")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}