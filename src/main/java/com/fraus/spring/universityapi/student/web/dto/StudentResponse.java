package com.fraus.spring.universityapi.student.web.dto;

import com.fraus.spring.universityapi.group.web.dto.GroupResponse;

public record StudentResponse(
        Long id,
        String lastName,
        String firstName,
        String patronymic,
        String phoneNumber,
        String email,
        GroupResponse group
) {
}
