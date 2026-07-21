package com.fraus.spring.universityapi.faculty.web.dto;

import jakarta.validation.constraints.*;

public record FacultyRequest(
        @NotNull(message = "Номер факультета обязателен для ввода")
        @Positive(message = "Номер факультета должен быть положительным")
        Short number,

        @Size(max = 60, message = "Название факультета не должно превышать 60 символов")
        @NotBlank(message = "Название факультета обязательно для ввода")
        String name
) {
}
