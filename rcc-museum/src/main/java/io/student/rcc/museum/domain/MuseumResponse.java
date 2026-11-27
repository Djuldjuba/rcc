package io.student.rcc.museum.domain;

public record MuseumResponse(
        String id,
        String title,
        String description,
        String photo,
        String city,
        String countryId,
        String countryName
) {}