package com.fraus.spring.universityapi.group.web.dto;

import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyResponse;

public record GroupResponse(
        Integer id,
        String name,
        Short course,
        SpecialtyResponse specialty
) {
}
