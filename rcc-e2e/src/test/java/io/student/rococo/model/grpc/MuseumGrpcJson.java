package io.student.rococo.model.grpc;

import io.student.rcc.museum.grpc.MuseumResponseProto;

public record MuseumGrpcJson(
        String id,
        String title,
        String description,
        String photo,
        String city,
        String countryId,
        String countryName
) {
    public static MuseumGrpcJson from(MuseumResponseProto p) {
        return new MuseumGrpcJson(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getPhoto(),
                p.getCity(),
                p.getCountryId(),
                p.getCountryName()
        );
    }
}