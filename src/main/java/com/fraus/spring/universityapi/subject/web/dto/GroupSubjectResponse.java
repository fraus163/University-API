package com.fraus.spring.universityapi.subject.web.dto;

import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.subject.domain.db.ControlType;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;

public record GroupSubjectResponse(
        Integer id,
        GroupResponse group,
        SubjectResponse subject,
        TeacherResponse teacher,
        Short term,
        Short hours,
        ControlType typeOfControl
) {
}
