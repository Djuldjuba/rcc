package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.student.rococo.data.entity.CountryEntity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record CountryJson(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name
) {
    public static @Nullable CountryJson fromEntity(CountryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CountryJson(
                entity.getId(),
                entity.getName()
        );
    }

    public CountryEntity toEntity() {
        CountryEntity entity = new CountryEntity();
        entity.setId(id);
        entity.setName(name);
        return entity;
    }
}