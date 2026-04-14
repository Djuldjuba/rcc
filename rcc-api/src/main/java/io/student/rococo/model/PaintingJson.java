package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaintingJson(UUID id, String title, String description, String content, ArtistJson artist,
                           MuseumJson museum) {

}
