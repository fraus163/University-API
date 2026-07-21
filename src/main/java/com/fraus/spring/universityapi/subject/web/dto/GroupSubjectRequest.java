package com.fraus.spring.universityapi.subject.web.dto;

import com.fraus.spring.universityapi.subject.domain.db.ControlType;
import jakarta.validation.constraints.NotNull;

public record GroupSubjectRequest(
        @NotNull(message = "Группа обязательна для ввода")
        Integer groupId,

        @NotNull(message = "Дисциплина обязательна для ввода")
        Integer subjectId,

        @NotNull(message = "Преподаватель обязателен для ввода")
        Long teacherId,

        @NotNull(message = "Номер семестра обязателен для ввода")
        Short term,

        @NotNull(message = "Количество часов обязательно для ввода")
        Short hours,

        @NotNull(message = "Вид контроля предмета обязателен для ввода")
        ControlType typeOfControl
) {
}
