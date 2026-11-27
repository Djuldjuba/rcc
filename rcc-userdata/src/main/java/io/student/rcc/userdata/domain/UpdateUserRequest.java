package io.student.rcc.userdata.domain;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 255, message = "Имя не может превышать 255 символов")
        String firstname,

        @Size(max = 255, message = "Фамилия не может превышать 255 символов")
        String lastname,

        String avatar
) {}