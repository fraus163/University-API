package com.fraus.spring.universityapi.position.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PositionRequest(
        @NotBlank(message = "Название должности обязательно для ввода")
        @Size(max = 60, message = "Название должности должно быть не более 60 символов")
        String name
) {
}
