package com.fraus.spring.universityapi.schedule.web.mapper;

import com.fraus.spring.universityapi.group.web.mapper.GroupMapper;
import com.fraus.spring.universityapi.schedule.domain.db.ScheduleEntity;
import com.fraus.spring.universityapi.schedule.web.dto.ScheduleRequest;
import com.fraus.spring.universityapi.schedule.web.dto.ScheduleResponse;
import com.fraus.spring.universityapi.subject.web.mapper.SubjectMapper;
import com.fraus.spring.universityapi.teacher.web.mapper.TeacherMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TeacherMapper.class, SubjectMapper.class, GroupMapper.class})
public interface ScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "groups", ignore = true)
    ScheduleEntity toEntity(ScheduleRequest request);

    ScheduleResponse toResponse(ScheduleEntity entity);

    List<ScheduleResponse> toResponseList(List<ScheduleEntity> entities);

    default Page<ScheduleResponse> toResponsePage(Page<ScheduleEntity> entities) {
        if (entities == null) {
            return Page.empty();
        }
        return entities.map(this::toResponse);
    }
}
