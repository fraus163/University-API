package com.fraus.spring.universityapi.department.web.mapper;

import com.fraus.spring.universityapi.department.domain.db.DepartmentEntity;
import com.fraus.spring.universityapi.department.web.dto.DepartmentRequest;
import com.fraus.spring.universityapi.department.web.dto.DepartmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "faculty", ignore = true)
    @Mapping(target = "departmentPositions", ignore = true)
    DepartmentEntity toEntity(DepartmentRequest dto);

    @Mapping(target = "facultyId", source = "faculty.id")
    DepartmentResponse toResponse(DepartmentEntity entity);

    List<DepartmentResponse> toResponseList(List<DepartmentEntity> entities);
}