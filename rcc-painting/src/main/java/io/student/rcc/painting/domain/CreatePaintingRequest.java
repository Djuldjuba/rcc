package io.student.rcc.painting.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePaintingRequest(
        @NotBlank(message = "Название картины не может быть пустым")
        @Size(max = 255, message = "Название картины не может превышать 255 символов")
        String title,

        @Size(max = 1000, message = "Описание не может превышать 1000 символов")
        String description,

        String content,

        @NotBlank(message = "ID художника не может быть пустым")
        String artistId,

        String museumId
) {}