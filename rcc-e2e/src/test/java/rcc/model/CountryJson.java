package rcc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import rcc.data.entity.CountryEntity;

import java.util.UUID;

public record CountryJson(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("name")
        String name
) {
    public static CountryJson fromEntity(CountryEntity entity) {
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
