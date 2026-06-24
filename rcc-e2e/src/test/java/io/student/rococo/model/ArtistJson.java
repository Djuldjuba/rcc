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
        return new ArtistJson(
                entity.getId(),
                entity.getName(),
                entity.getBiography(),
                entity.getPhoto() != null ? new String(entity.getPhoto()) : null
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