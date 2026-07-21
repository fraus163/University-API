package com.fraus.spring.universityapi.teacher.web.dto;

import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionResponse;
import com.fraus.spring.universityapi.teacher.domain.db.AcademicDegreeType;
import com.fraus.spring.universityapi.teacher.domain.db.AcademicRankType;

import java.util.List;

public record TeacherResponse (
        Long id,
        String lastName,
        String firstName,
        String patronymic,
        String phoneNumber,
        String email,
        Short experience,
        AcademicRankType academicRank,
        AcademicDegreeType academicDegree,
        List<DepartmentPositionResponse> positions
) {
}
