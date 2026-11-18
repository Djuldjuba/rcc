package io.student.rcc.painting.domain;

public record PaintingResponse(
        String id,
        String title,
        String description,
        String content,
        String artistId,
        String artistName,
        String museumId,
        String museumTitle
) {}