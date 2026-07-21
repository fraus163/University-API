package com.fraus.spring.universityapi.subject.web.mapper;

import com.fraus.spring.universityapi.group.web.mapper.GroupMapper;
import com.fraus.spring.universityapi.subject.domain.db.GroupSubjectEntity;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectResponse;
import com.fraus.spring.universityapi.teacher.web.mapper.TeacherMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {GroupMapper.class, SubjectMapper.class, TeacherMapper.class})
public interface GroupSubjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "scoreSheets", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    GroupSubjectEntity toEntity(GroupSubjectRequest request);

    GroupSubjectResponse toResponse(GroupSubjectEntity entity);

    List<GroupSubjectResponse> toResponseList(List<GroupSubjectEntity> entities);

    default Page<GroupSubjectResponse> toResponsePage(Page<GroupSubjectEntity> entities) {
        if (entities == null) {
            return Page.empty();
        }
        return entities.map(this::toResponse);
    }
}
