package com.fraus.spring.universityapi.student.web.dto;

import jakarta.validation.constraints.NotNull;

public record StudentUpdateRequest(
        @NotNull(message = "Группа студента обязательна для ввода")
        Integer groupId
) {
}
