package com.fraus.spring.universityapi.teacher.web.dto;

import com.fraus.spring.universityapi.teacher.domain.db.AcademicDegreeType;
import com.fraus.spring.universityapi.teacher.domain.db.AcademicRankType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record TeacherUpdateRequest(
        @PositiveOrZero(message = "Стаж не должен быть отрицательным")
        Short experience,

        AcademicRankType academicRank,

        AcademicDegreeType academicDegree,

        @NotNull(message = "Должности обязательны для ввода")
        List<Integer> positionIds
) {
}
