package com.fraus.spring.universityapi.position.web.dto;

import jakarta.validation.constraints.NotNull;

public record DepartmentPositionRequest(
        @NotNull(message = "Должность обязательна для ввода")
        Short positionId,

        @NotNull(message = "Кафедра обязательна для ввода")
        Short departmentId
) {
}
