package com.fraus.spring.universityapi.schedule.web.dto;

import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.subject.web.dto.SubjectResponse;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ScheduleResponse(
        Long id,
        TeacherResponse teacher,
        SubjectResponse subject,
        LocalTime timeFrom,
        LocalTime timeTo,
        LocalDate date,
        String audience,
        List<GroupResponse> groups
) {
}
