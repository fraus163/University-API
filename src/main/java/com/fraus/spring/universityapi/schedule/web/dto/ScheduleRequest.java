package com.fraus.spring.universityapi.schedule.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ScheduleRequest(
        @NotNull(message = "Преподаватель обязателен для ввода")
        Long teacherId,

        @NotNull(message = "Предмет обязателен для ввода")
        Integer subjectId,

        @NotNull(message = "Время начала занятия обязательно для ввода")
        LocalTime timeFrom,

        @NotNull(message = "Время конца занятия обязательно для ввода")
        LocalTime timeTo,

        @NotNull(message = "Дата занятия обязательна для ввода")
        @FutureOrPresent(message = "Дата должна быть в будущем или настоящем времени")
        LocalDate date,

        @NotNull(message = "Аудитория обязательна для ввода")
        String audience,

        @NotNull(message = "Группы обязательны для ввода")
        List<Integer> groupIds
) {
        public ScheduleRequest {
                if (timeFrom != null && timeTo != null && !timeFrom.isBefore(timeTo)) {
                        throw new IllegalArgumentException("Время начала занятия должно быть раньше времени окончания");
                }
        }
}
