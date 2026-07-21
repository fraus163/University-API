package com.fraus.spring.universityapi.department.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotNull(message = "Факультет обязателен для ввода")
        Short facultyId,

        @NotBlank(message = "Название кафедры обязательно для ввода")
        @Size(max = 60, message = "Название кафедры должно быть не более 60 символов")
        String name
) {
}