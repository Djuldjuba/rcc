package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.student.rococo.data.entity.MuseumEntity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record MuseumJson(
        @JsonProperty("id") UUID id,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("city") String city,
        @JsonProperty("photo") String photo,
        @JsonProperty("country") CountryJson country
) {
    public static @Nullable MuseumJson fromEntity(MuseumEntity entity) {
        if (entity == null) {
            return null;
        }
        return new MuseumJson(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCity(),
                entity.getPhoto() != null ? new String(entity.getPhoto()) : null,
                CountryJson.fromEntity(entity.getCountry())
        );
    }

    public MuseumEntity toEntity() {
        MuseumEntity entity = new MuseumEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setCity(city);
        if (photo != null) {
            entity.setPhoto(photo.getBytes());
        }
        if (country != null) {
            entity.setCountry(country.toEntity());
        }
        return entity;
    }
}