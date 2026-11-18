package io.student.rcc.gateway.service;

import io.student.rcc.gateway.model.UserJson;
import io.student.rcc.userdata.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private UserdataServiceGrpc.UserdataServiceBlockingStub stub;

    private UserClient client;

    @BeforeEach
    void setUp() {
        client = new UserClient();
        ReflectionTestUtils.setField(client, "stub", stub);
    }

    @Test
    void getCurrentUserShouldMap() {
        when(stub.getCurrentUser(any(EmptyRequest.class)))
                .thenReturn(UserResponseProto.newBuilder()
                        .setId("u-1").setUsername("alice").setFirstname("Alice").build());

        UserJson result = client.getCurrentUser();

        assertEquals("u-1", result.id());
        assertEquals("alice", result.username());
        assertEquals("Alice", result.firstname());
    }

    @Test
    void updateUserShouldMapNonNullFields() {
        when(stub.updateUser(any(UpdateUserRequestProto.class)))
                .thenReturn(UserResponseProto.newBuilder()
                        .setId("u-1").setUsername("alice").setFirstname("New").build());

        UserJson result = client.updateUser(new UserJson(null, null, "New", null, null));

        assertEquals("New", result.firstname());
        verify(stub).updateUser(argThat(r ->
                r.getFirstname().equals("New")
                        && r.getLastname().isEmpty()
                        && r.getAvatar().isEmpty()));
    }
}