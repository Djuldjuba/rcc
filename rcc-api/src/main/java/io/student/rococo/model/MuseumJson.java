package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.student.rococo.data.entity.MuseumEntity;
import io.student.rococo.util.BytesAsString;
import io.student.rococo.util.StringAsBytes;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MuseumJson(UUID id, String title, String description, String photo, GeoJson geo) {

  public static MuseumJson fromEntity(MuseumEntity entity) {
    return new MuseumJson(
        entity.getId(),
        entity.getTitle(),
        entity.getDescription(),
        new BytesAsString(entity.getPhoto()).string(),
        new GeoJson(
            entity.getCity(),
            new CountryJson(
                entity.getCountry().getId(),
                entity.getCountry().getName()
            )
        )
    );
  }

  public MuseumEntity toEntity() {
    MuseumEntity entity = new MuseumEntity();
    entity.setTitle(title);
    entity.setDescription(description);
    entity.setPhoto(
        new StringAsBytes(
            photo
        ).bytes()
    );
    entity.setCity(geo.city());
    return entity;
  }
}
