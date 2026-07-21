package com.fraus.spring.universityapi.specialty.web.mapper;

import com.fraus.spring.universityapi.faculty.web.mapper.FacultyMapper; // Импортируем маппер факультета
import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyRequest;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {FacultyMapper.class})
public interface SpecialtyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "faculty", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "applicants", ignore = true)
    SpecialtyEntity toEntity(SpecialtyRequest dto);

    SpecialtyResponse toResponse(SpecialtyEntity entity);

    List<SpecialtyResponse> toResponseList(List<SpecialtyEntity> entities);

    default Page<SpecialtyResponse> toResponsePage(Page<SpecialtyEntity> entitiesPage) {
        if (entitiesPage == null) {
            return Page.empty();
        }
        return entitiesPage.map(this::toResponse);
    }
}