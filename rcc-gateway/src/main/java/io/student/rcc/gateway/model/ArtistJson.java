package io.student.rcc.gateway.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.student.rcc.artist.grpc.ArtistResponseProto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ArtistJson(
        String id,

        @NotBlank(message = "Имя художника не может быть пустым")
        @Size(max = 255, message = "Имя художника не может превышать 255 символов")
        String name,

        @Size(max = 2000, message = "Биография не может превышать 2000 символов")
        String biography,

        String photo
) {
        public static ArtistJson fromGrpc(ArtistResponseProto p) {
                return new ArtistJson(
                        emptyToNull(p.getId()),
                        emptyToNull(p.getName()),
                        emptyToNull(p.getBiography()),
                        emptyToNull(p.getPhoto())
                );
        }

        private static String emptyToNull(String value) {
                return (value == null || value.isEmpty()) ? null : value;
        }
}