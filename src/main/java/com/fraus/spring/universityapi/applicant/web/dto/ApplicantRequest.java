package com.fraus.spring.universityapi.applicant.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ApplicantRequest(
        @NotNull(message = "Баллы обязательны для ввода")
        @PositiveOrZero(message = "Баллы не должны быть отрицательными")
        @Max(value = 500, message = "Баллы не могут превышать 500")
        Short scores,

        @NotNull(message = "Специальность обязательная для ввода")
        Short specialtyId
) {
}
