package com.fraus.spring.universityapi.specialty.web.dto;

import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SpecialtyRequest(
        @NotBlank(message = "Название специальности не должно быть пустым")
        @Size(max = 30, message = "Название специальности не должно превышать 30 символов")
        String name,

        @NotNull(message = "Уровень образование не должен быть пустым")
        DegreeType degree,

        @NotNull(message = "Факультет обязателен для ввода")
        Short facultyId
) {
}
