package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.student.rococo.data.entity.ArtistEntity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record ArtistJson(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("biography") String biography,
        @JsonProperty("photo") String photo
) {
    public static @Nullable ArtistJson fromEntity(ArtistEntity entity) {
        if (entity == null) {
            return null;
        }

        String photo = null;
        if (entity.getPhoto() != null) {
            String raw = new String(entity.getPhoto());
            if (!raw.startsWith("data:image")) {
                photo = "data:image/jpeg;base64," + raw;
            } else {
                photo = raw;
            }
        }

        return new ArtistJson(
                entity.getId(),
                entity.getName(),
                entity.getBiography(),
                photo
        );
    }

    public ArtistEntity toEntity() {
        ArtistEntity entity = new ArtistEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setBiography(biography);
        if (photo != null) {
            entity.setPhoto(photo.getBytes());
        }
        return entity;
    }
}