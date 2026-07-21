package com.fraus.spring.universityapi.scoresheet.web.dto;

import com.fraus.spring.universityapi.student.web.dto.StudentResponse;
import com.fraus.spring.universityapi.scoresheet.domain.db.AssessmentType;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectResponse;

public record ScoreSheetResponse(
        Long id,
        StudentResponse student,
        GroupSubjectResponse subject,
        AssessmentType assessment
) {
}
