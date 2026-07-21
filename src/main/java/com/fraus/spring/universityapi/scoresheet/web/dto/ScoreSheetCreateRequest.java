package com.fraus.spring.universityapi.scoresheet.web.dto;

import com.fraus.spring.universityapi.scoresheet.domain.db.AssessmentType;
import jakarta.validation.constraints.NotNull;

public record ScoreSheetCreateRequest(
        @NotNull(message = "Студент обязателен для ввода")
        Long studentId,

        @NotNull(message = "Дисциплина обязательна для ввода")
        Integer groupSubjectId,

        @NotNull(message = "Оценка обязательна для ввода")
        AssessmentType assessment
) {
}
