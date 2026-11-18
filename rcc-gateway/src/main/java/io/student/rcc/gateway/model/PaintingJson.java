package io.student.rcc.gateway.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.student.rcc.painting.grpc.PaintingResponseProto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaintingJson(
        String id,
        String title,
        String description,
        String content,
        ArtistJson artist,
        MuseumJson museum
) {
    public record ArtistJson(
            String id,
            String name
    ) {}

    public record MuseumJson(
            String id,
            String title
    ) {}

    public static PaintingJson fromGrpc(PaintingResponseProto p) {
        return new PaintingJson(
                emptyToNull(p.getId()),
                emptyToNull(p.getTitle()),
                emptyToNull(p.getDescription()),
                emptyToNull(p.getContent()),
                p.getArtistId().isEmpty()
                        ? null
                        : new ArtistJson(
                        p.getArtistId(),
                        emptyToNull(p.getArtistName())
                ),
                p.getMuseumId().isEmpty()
                        ? null
                        : new MuseumJson(
                        p.getMuseumId(),
                        emptyToNull(p.getMuseumTitle())
                )
        );
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}