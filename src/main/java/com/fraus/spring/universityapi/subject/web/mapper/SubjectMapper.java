package com.fraus.spring.universityapi.subject.web.mapper;

import com.fraus.spring.universityapi.subject.domain.db.SubjectEntity;
import com.fraus.spring.universityapi.subject.web.dto.SubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.SubjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groupSubjects", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    SubjectEntity toEntity(SubjectRequest dto);

    SubjectResponse toResponse(SubjectEntity entity);

    List<SubjectRequest> toResponseList(List<SubjectEntity> entities);

    default Page<SubjectResponse> toResponsePage(Page<SubjectEntity> entities) {
        if (entities == null) {
            return Page.empty();
        }
        return entities.map(this::toResponse);
    }
}
