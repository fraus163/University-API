package com.fraus.spring.universityapi.group.web.mapper;

import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.group.web.dto.GroupRequest;
import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.specialty.web.mapper.SpecialtyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SpecialtyMapper.class})
public interface GroupMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    @Mapping(target = "groupSubjects", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    GroupEntity toEntity(GroupRequest request);

    GroupResponse toResponse(GroupEntity entity);

    List<GroupResponse> toResponseList(List<GroupEntity> entities);

    default Page<GroupResponse> toResponsePage(Page<GroupEntity> entities) {
        if (entities == null) {
            return Page.empty();
        }
        return entities.map(this::toResponse);
    }
}
