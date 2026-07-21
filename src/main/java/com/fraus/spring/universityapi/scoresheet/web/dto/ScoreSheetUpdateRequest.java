package com.fraus.spring.universityapi.scoresheet.web.dto;

import com.fraus.spring.universityapi.scoresheet.domain.db.AssessmentType;
import jakarta.validation.constraints.NotNull;

public record ScoreSheetUpdateRequest(
        @NotNull(message = "Оценка обязательна для ввода")
        AssessmentType assessment
) {
}
