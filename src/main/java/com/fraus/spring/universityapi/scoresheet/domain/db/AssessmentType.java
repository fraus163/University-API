package com.fraus.spring.universityapi.scoresheet.domain.db;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssessmentType {
    EXCELLENT("Отлично"),
    GOOD("Хорошо"),
    SATISFACTORY("Удовлетворительно"),
    UNSATISFACTORY("Неудовлетворительно"),

    PASSED("Зачтено"),
    NOT_PASSED("Незачтено");

    private final String title;
}