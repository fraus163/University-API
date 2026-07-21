package com.fraus.spring.universityapi.subject.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SubjectRequest(
        @NotBlank(message = "Название дисциплины обязательно для ввода")
        String name,

        @NotBlank(message = "Описание дисциплины обязательно для ввода")
        String description
) {
}
