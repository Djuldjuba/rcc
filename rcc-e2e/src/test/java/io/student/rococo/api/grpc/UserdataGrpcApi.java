package io.student.rococo.api.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.qameta.allure.Step;
import io.student.rcc.userdata.grpc.EmptyRequest;
import io.student.rcc.userdata.grpc.UpdateUserRequestProto;
import io.student.rcc.userdata.grpc.UserResponseProto;
import io.student.rcc.userdata.grpc.UserdataServiceGrpc;
import org.junit.jupiter.api.Assertions;

import static io.student.rococo.api.grpc.utils.GrpcUtils.nullToEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserdataGrpcApi {

    private static final Metadata.Key<String> AUTH_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final UserdataServiceGrpc.UserdataServiceBlockingStub baseStub;

    public UserdataGrpcApi(ManagedChannel channel) {
        this.baseStub = UserdataServiceGrpc.newBlockingStub(channel);
    }

    private UserdataServiceGrpc.UserdataServiceBlockingStub stubWithToken(String token) {
        if (token == null) {
            return baseStub;
        }
        Metadata metadata = new Metadata();
        metadata.put(AUTH_KEY, "Bearer " + token);
        return baseStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
    }

    @Step("Получить профиль текущего пользователя")
    public UserResponseProto getCurrentUser(String token) {
        return stubWithToken(token).getCurrentUser(EmptyRequest.newBuilder().build());
    }

    @Step("Обновить профиль пользователя: имя '{firstname}', фамилия '{lastname}'")
    public UserResponseProto updateUser(String token, String firstname, String lastname, String avatar) {
        return stubWithToken(token).updateUser(
                UpdateUserRequestProto.newBuilder()
                        .setFirstname(nullToEmpty(firstname))
                        .setLastname(nullToEmpty(lastname))
                        .setAvatar(nullToEmpty(avatar))
                        .build()
        );
    }

    @Step("Проверить, что профиль содержит имя '{firstname}' и фамилию '{lastname}'")
    public UserdataGrpcApi checkUser(UserResponseProto user, String firstname, String lastname) {
        Assertions.assertAll(
                () -> assertEquals(firstname, user.getFirstname(), "Некорректное имя"),
                () -> assertEquals(lastname, user.getLastname(), "Некорректная фамилия")
        );
        return this;
    }

    @Step("Проверить, что запрос завершился с ошибкой {expectedCode}")
    public UserdataGrpcApi checkErrorStatus(Runnable action, Status.Code expectedCode) {
        StatusRuntimeException ex = Assertions.assertThrows(StatusRuntimeException.class, action::run);
        assertEquals(expectedCode, ex.getStatus().getCode(), "Неверный статус ошибки gRPC");
        return this;
    }
}