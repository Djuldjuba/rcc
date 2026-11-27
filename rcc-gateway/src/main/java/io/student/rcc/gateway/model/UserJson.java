package io.student.rcc.gateway.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.student.rcc.userdata.grpc.UserResponseProto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserJson(
        String id,
        String username,
        String firstname,
        String lastname,
        String avatar
) {
    public static UserJson fromGrpc(UserResponseProto p) {
        return new UserJson(
                emptyToNull(p.getId()),
                emptyToNull(p.getUsername()),
                emptyToNull(p.getFirstname()),
                emptyToNull(p.getLastname()),
                emptyToNull(p.getAvatar())
        );
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}