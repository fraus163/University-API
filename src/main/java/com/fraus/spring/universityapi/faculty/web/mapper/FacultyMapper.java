package com.fraus.spring.universityapi.faculty.web.mapper;

import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyRequest;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FacultyMapper {
    FacultyEntity toEntity(FacultyRequest dto);
    FacultyResponse toResponse(FacultyEntity entity);
    List<FacultyResponse> toResponseList(List<FacultyEntity> entities);
}
