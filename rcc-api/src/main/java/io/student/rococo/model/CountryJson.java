package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.student.rococo.data.entity.CountryEntity;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CountryJson(UUID id, String name) {
  public static CountryJson fromEntity(CountryEntity entity) {
    return (entity != null)
        ? new CountryJson(
        entity.getId(),
        entity.getName()
    ) : null;
  }

  public CountryEntity toEntity() {
    CountryEntity entity = new CountryEntity();
    entity.setName(name);
    return entity;
  }
}
