package io.student.rococo.model;

import io.student.rococo.data.entity.UserEntity;

import java.util.Base64;
import java.util.UUID;

public record UserJson(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String avatar) {

    public static UserJson fromEntity(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        String avatarString = null;
        if (entity.getAvatar() != null) {
            avatarString = Base64.getEncoder().encodeToString(entity.getAvatar());
        }

        return new UserJson(
                entity.getId(),
                entity.getUsername(),
                entity.getFirstname(),
                entity.getLastname(),
                avatarString
        );
    }
}