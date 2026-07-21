package com.fraus.spring.universityapi.student.web.mapper;

import com.fraus.spring.universityapi.group.web.mapper.GroupMapper;
import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
import com.fraus.spring.universityapi.student.web.dto.StudentCreateRequest;
import com.fraus.spring.universityapi.student.web.dto.StudentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {GroupMapper.class})
public interface StudentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "scoreSheets", ignore = true)
    StudentEntity toEntity(StudentCreateRequest request);

    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "patronymic", source = "user.patronymic")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "email", source = "user.email")
    StudentResponse toResponse(StudentEntity entity);

    List<StudentResponse> toResponseList(List<StudentEntity> entities);

    default Page<StudentResponse> toResponsePage(Page<StudentEntity> entities) {
        if (entities == null) {
            return Page.empty();
        }
        return entities.map(this::toResponse);
    }
}
