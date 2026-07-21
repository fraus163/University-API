package com.fraus.spring.universityapi.group.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GroupRequest(
        @NotBlank(message = "Название группы не должно быть пустым")
        @Size(max = 10, message = "Название группы не должно превышать 10 символов")
        String name,

        @NotNull(message = "Номер курса обязателен для ввода")
        Short course,

        @NotNull(message = "Специальность обязательна для ввода")
        Short specialtyId
) {
}
