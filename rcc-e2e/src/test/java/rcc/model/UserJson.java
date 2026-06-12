package rcc.model;

import java.util.UUID;

public record UserJson(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String avatar) {
}
