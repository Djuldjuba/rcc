package io.student.rcc.artist.domain;

public record ArtistResponse(
        String id,
        String name,
        String biography,
        String photo
) {}