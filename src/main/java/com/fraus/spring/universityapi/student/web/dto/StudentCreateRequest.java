package com.fraus.spring.universityapi.student.web.dto;

import jakarta.validation.constraints.NotNull;

public record StudentCreateRequest(
        @NotNull(message = "Пользователь обязателен для ввода")
        Long userId,

        @NotNull(message = "Группа студента обязательна для ввода")
        Integer groupId
) {
}
