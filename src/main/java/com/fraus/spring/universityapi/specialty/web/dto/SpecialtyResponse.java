package com.fraus.spring.universityapi.specialty.web.dto;

import com.fraus.spring.universityapi.faculty.web.dto.FacultyResponse;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;

public record SpecialtyResponse(
        Short id,
        String name,
        DegreeType degree,
        FacultyResponse faculty
) {
}
