package com.fraus.spring.universityapi.teacher.web.mapper;

import com.fraus.spring.universityapi.position.web.mapper.DepartmentPositionMapper;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherCreateRequest;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DepartmentPositionMapper.class})
public interface TeacherMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "positions", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    TeacherEntity toEntity(TeacherCreateRequest request);

    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "patronymic", source = "user.patronymic")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "email", source = "user.email")
    TeacherResponse toResponse(TeacherEntity entity);

    List<TeacherResponse> toResponseList(List<TeacherEntity> entities);

    default Page<TeacherResponse> toResponsePage(Page<TeacherEntity> entities) {
        if (entities == null) {
            return Page.empty();
        }
        return entities.map(this::toResponse);
    }
}
