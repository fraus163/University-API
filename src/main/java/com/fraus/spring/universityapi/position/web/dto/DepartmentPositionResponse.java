package com.fraus.spring.universityapi.position.web.dto;

import com.fraus.spring.universityapi.department.web.dto.DepartmentResponse;

public record DepartmentPositionResponse(
        Integer id,
        PositionResponse position,
        DepartmentResponse department
) {
}
