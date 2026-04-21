package io.student.rcc.model;

import java.util.UUID;

public record UserJson(
        UUID id,
        String username,
        String firstname,
        String lastname,
        String avatar) {
}
