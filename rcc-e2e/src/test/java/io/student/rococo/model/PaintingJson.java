package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.student.rococo.data.entity.ArtistEntity;
import io.student.rococo.data.entity.MuseumEntity;
import io.student.rococo.data.entity.PaintingEntity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record PaintingJson(
        @JsonProperty("id") UUID id,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("content") String content,
        @JsonProperty("artist") ArtistJson artist,
        @JsonProperty("museum") MuseumJson museum
) {
    public static @Nullable PaintingJson fromEntity(PaintingEntity entity) {
        return fromEntity(entity, null, null);
    }

    public static @Nullable PaintingJson fromEntity(PaintingEntity entity,
                                                    @Nullable ArtistEntity artist,
                                                    @Nullable MuseumEntity museum) {
        if (entity == null) {
            return null;
        }
        return new PaintingJson(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getContent() != null ? new String(entity.getContent()) : null,
                ArtistJson.fromEntity(artist),
                MuseumJson.fromEntity(museum)
        );
    }

    public PaintingEntity toEntity() {
        PaintingEntity entity = new PaintingEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setDescription(description);
        if (content != null) {
            entity.setContent(content.getBytes());
        }
        if (artist != null) {
            entity.setArtistId(artist.id());
        }
        if (museum != null) {
            entity.setMuseumId(museum.id());
        }
        return entity;
    }
}