package com.fraus.spring.universityapi.scoresheet.web.mapper;

import com.fraus.spring.universityapi.scoresheet.domain.db.ScoreSheetEntity;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetCreateRequest;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetResponse;
import com.fraus.spring.universityapi.student.web.mapper.StudentMapper;
import com.fraus.spring.universityapi.subject.web.mapper.GroupSubjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StudentMapper.class, GroupSubjectMapper.class})
public interface ScoreSheetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "subject", ignore = true)
    ScoreSheetEntity toEntity(ScoreSheetCreateRequest request);

    ScoreSheetResponse toResponse(ScoreSheetEntity entity);

    List<ScoreSheetResponse> toResponseList(List<ScoreSheetEntity> entities);

    default Page<ScoreSheetResponse> toResponsePage(Page<ScoreSheetEntity> entities) {
        if (entities == null) {
            return Page.empty();
        }
        return entities.map(this::toResponse);
    }
}
