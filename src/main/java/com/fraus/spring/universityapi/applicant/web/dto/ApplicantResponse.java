package com.fraus.spring.universityapi.applicant.web.dto;

import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyResponse;

public record ApplicantResponse(
        Long id,
        String lastName,
        String firstName,
        String patronymic,
        String phoneNumber,
        String email,
        Short scores,
        SpecialtyResponse specialty
) {
}
