package io.student.rcc.userdata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.student.rcc.userdata.domain.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    private static final String FIXTURE =
            "/io/student/rcc/userdata/controller/UserControllerTest.sql";

    private static final String USER_ALICE = "alice";
    private static final String ID_ALICE  = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper om = new ObjectMapper();

    @Test
    @Sql(FIXTURE)
    void shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject(USER_ALICE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_ALICE))
                .andExpect(jsonPath("$.username").value(USER_ALICE))
                .andExpect(jsonPath("$.firstname").value("Alice"))
                .andExpect(jsonPath("$.lastname").value("Ivanova"));
    }

    @Test
    void shouldCreateUserOnFirstRequest() throws Exception {
        mockMvc.perform(get("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject("newcomer"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newcomer"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldReturn401WhenGetWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Sql(FIXTURE)
    void shouldUpdateUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Alice-New", "Sidorova", null);

        mockMvc.perform(post("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject(USER_ALICE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USER_ALICE))
                .andExpect(jsonPath("$.firstname").value("Alice-New"))
                .andExpect(jsonPath("$.lastname").value("Sidorova"));
    }

    @Test
    @Sql(FIXTURE)
    void shouldKeepFieldsWhenRequestHasNulls() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null);

        mockMvc.perform(post("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject(USER_ALICE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Alice"))
                .andExpect(jsonPath("$.lastname").value("Ivanova"));
    }

    @Test
    void shouldReturn400WhenFirstnameTooLong() throws Exception {
        String tooLong = "x".repeat(256);
        UpdateUserRequest request = new UpdateUserRequest(tooLong, null, null);

        mockMvc.perform(post("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject(USER_ALICE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenUpdateWithoutAuth() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("A", "B", null);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Sql(FIXTURE)
    void shouldReturn404WhenUpdatingUnknownUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("A", "B", null);

        mockMvc.perform(post("/api/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j.subject("ghost")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}