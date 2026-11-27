package io.student.rococo.test.grpc;

import io.grpc.Status;
import io.student.rococo.api.grpc.UserdataGrpcApi;
import io.student.rococo.api.grpc.core.GrpcStubs;
import io.student.rococo.jupiter.annotation.ApiLoginGrpc;
import io.student.rococo.jupiter.annotation.Token;
import io.student.rococo.jupiter.annotation.User;
import io.student.rcc.userdata.grpc.UserResponseProto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserdataGrpcTest {

    private static final UserdataGrpcApi userdataApi = GrpcStubs.userdata();

    @Test
    @User
    @ApiLoginGrpc
    @DisplayName("gRPC: Проверка получения профиля текущего пользователя")
    void getCurrentUser(@Token String token) {
        UserResponseProto user = userdataApi.getCurrentUser(token);

        assertNotNull(user.getId(), "id должен быть сгенерирован");
        assertNotNull(user.getUsername(), "username должен быть заполнен");
    }

    @Test
    @User
    @ApiLoginGrpc
    @DisplayName("gRPC: Проверка обновления профиля пользователя")
    void updateUser(@Token String token) {
        UserResponseProto updated = userdataApi.updateUser(
                token, "Алексей", "Носков", null
        );

        userdataApi.checkUser(updated, "Алексей", "Носков");
    }

    @Test
    @User
    @ApiLoginGrpc
    @DisplayName("gRPC: Проверка, что профиль обновляется корректно и сохраняется")
    void checkProfileUpdatePersists(@Token String token) {
        userdataApi.updateUser(token, "Иван", "Иванов", null);

        UserResponseProto actual = userdataApi.getCurrentUser(token);

        userdataApi.checkUser(actual, "Иван", "Иванов");
    }

    @Test
    @DisplayName("gRPC: Проверка получения профиля без токена авторизации")
    void getCurrentUserUnauthorized() {
        userdataApi.checkErrorStatus(
                () -> userdataApi.getCurrentUser(null),
                Status.Code.UNAUTHENTICATED
        );
    }

    @Test
    @User
    @ApiLoginGrpc
    @DisplayName("gRPC: Проверка ошибки при передаче невалидного токена")
    void getCurrentUserInvalidToken() {
        userdataApi.checkErrorStatus(
                () -> userdataApi.getCurrentUser("invalid.jwt.token"),
                Status.Code.UNAUTHENTICATED
        );
    }
}