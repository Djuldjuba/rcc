package io.student.rcc.userdata.domain;

public record UserResponse(
        String id,
        String username,
        String firstname,
        String lastname,
        String avatar
) {}