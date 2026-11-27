package io.student.rcc.gateway.service;

import io.student.rcc.gateway.model.UserJson;
import io.student.rcc.userdata.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class UserClient {

    @GrpcClient("userdata-service")
    private UserdataServiceGrpc.UserdataServiceBlockingStub stub;

    public UserJson getCurrentUser() {
        return UserJson.fromGrpc(
                stub.getCurrentUser(EmptyRequest.newBuilder().build())
        );
    }

    public UserJson updateUser(UserJson user) {
        UpdateUserRequestProto request = UpdateUserRequestProto.newBuilder()
                .setFirstname(nullToEmpty(user.firstname()))
                .setLastname(nullToEmpty(user.lastname()))
                .setAvatar(nullToEmpty(user.avatar()))
                .build();

        return UserJson.fromGrpc(stub.updateUser(request));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}