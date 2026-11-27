package io.student.rcc.gateway.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.student.rcc.museum.grpc.MuseumResponseProto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MuseumJson(
        String id,

        @NotBlank(message = "Название музея не может быть пустым")
        @Size(max = 255)
        String title,

        @Size(max = 1000)
        String description,

        String photo,
        Geo geo
) {
        public record Geo(
                String city,
                Country country
        ) {}

        public record Country(
                String id,
                String name
        ) {}

        public static MuseumJson fromGrpc(MuseumResponseProto p) {
                return new MuseumJson(
                        emptyToNull(p.getId()),
                        emptyToNull(p.getTitle()),
                        emptyToNull(p.getDescription()),
                        emptyToNull(p.getPhoto()),
                        new Geo(
                                emptyToNull(p.getCity()),
                                new Country(
                                        emptyToNull(p.getCountryId()),
                                        emptyToNull(p.getCountryName())
                                )
                        )
                );
        }

        private static String emptyToNull(String value) {
                return (value == null || value.isEmpty()) ? null : value;
        }
}