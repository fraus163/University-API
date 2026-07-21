package com.fraus.spring.universityapi.department.web.dto;

public record DepartmentResponse(
        Short id,
        String name,
        Short facultyId
) {
}
