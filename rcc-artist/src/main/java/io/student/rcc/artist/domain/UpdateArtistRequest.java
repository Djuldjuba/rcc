package io.student.rcc.artist.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateArtistRequest(
        @NotBlank(message = "Имя художника не может быть пустым")
        @Size(max = 255, message = "Имя художника не может превышать 255 символов")
        String name,

        @NotBlank(message = "Биография не может быть пустой")
        @Size(max = 2000, message = "Биография не может превышать 2000 символов")
        String biography,

        String photo
) {}