package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        if (entity == null) {
            return null;
        }
        return new PaintingJson(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getContent() != null ? new String(entity.getContent()) : null,
                ArtistJson.fromEntity(entity.getArtist()),
                MuseumJson.fromEntity(entity.getMuseum())
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
            entity.setArtist(artist.toEntity());
        }
        if (museum != null) {
            entity.setMuseum(museum.toEntity());
        }
        return entity;
    }
}