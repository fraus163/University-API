package com.fraus.spring.universityapi.schedule.domain;

import com.fraus.spring.universityapi.globalexception.customexception.AlreadyExistsException;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.domain.GroupRepository;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.schedule.domain.db.ScheduleEntity;
import com.fraus.spring.universityapi.schedule.web.dto.ScheduleRequest;
import com.fraus.spring.universityapi.schedule.web.dto.ScheduleResponse;
import com.fraus.spring.universityapi.schedule.web.mapper.ScheduleMapper;
import com.fraus.spring.universityapi.subject.domain.GroupSubjectRepository;
import com.fraus.spring.universityapi.subject.domain.SubjectRepository;
import com.fraus.spring.universityapi.teacher.domain.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final GroupRepository groupRepository;
    private final GroupSubjectRepository groupSubjectRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ScheduleResponse createSchedule(
            ScheduleRequest request
    ) {
        validateAudienceCollision(null, request);

        var scheduleToSave = scheduleMapper.toEntity(request);
        initSchedule(scheduleToSave, request);

        var createdSchedule = scheduleRepository.save(scheduleToSave);
        return scheduleMapper.toResponse(createdSchedule);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ScheduleResponse getScheduleById(
            Long id
    ) {
        return scheduleRepository
                .findById(id)
                .map(scheduleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Расписание не найдено",
                        "Schedule not found: scheduleId=" + id
                ));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Page<ScheduleResponse> getSchedulesByFilter(
            Pageable pageable,
            Long teacherId,
            Integer subjectId,
            LocalDate date,
            String audience,
            Integer groupId
    ) {
        var foundedSchedules = scheduleRepository.findSchedulesByFilter(
                pageable,
                teacherId,
                subjectId,
                date,
                audience,
                groupId
        );
        return scheduleMapper.toResponsePage(foundedSchedules);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ScheduleResponse updateScheduleById(
            Long id,
            ScheduleRequest request
    ) {
        var foundedSchedule = scheduleRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Расписание не найдено",
                        "Schedule not found: scheduleId=" + id
                ));

        validateAudienceCollision(id, request);

        var oldGroups = new ArrayList<>(foundedSchedule.getGroups());
        for (var group : oldGroups) {
            foundedSchedule.removeGroup(group);
        }

        initSchedule(foundedSchedule, request);

        foundedSchedule.setTimeFrom(request.timeFrom());
        foundedSchedule.setTimeTo(request.timeTo());
        foundedSchedule.setDate(request.date());
        foundedSchedule.setAudience(request.audience());

        return scheduleMapper.toResponse(foundedSchedule);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteScheduleById(Long id) {
        var foundedSchedule = scheduleRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Расписание не найдено",
                        "Schedule not found: scheduleId=" + id
                ));

        var oldGroups = new ArrayList<>(foundedSchedule.getGroups());
        for (GroupEntity group : oldGroups) {
            foundedSchedule.removeGroup(group);
        }

        scheduleRepository.delete(foundedSchedule);
    }

    private void initSchedule(
            ScheduleEntity entity,
            ScheduleRequest request
    ) {
        var foundedTeacher = teacherRepository
                .findById(request.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Преподаватель не найден",
                        "Teacher not found: teacherId=" + request.teacherId()
                ));
        var foundedSubject = subjectRepository
                .findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Дисциплина не найдена",
                        "Subject not found: subjectId=" + request.subjectId()
                ));

        entity.setTeacher(foundedTeacher);
        entity.setSubject(foundedSubject);

        for (var groupId : request.groupIds()) {
            var foundedGroup = groupRepository
                    .findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Группа не найдена",
                            "Group not found: groupId=" + groupId
                    ));

            boolean isAssigned = groupSubjectRepository.existsByGroup_IdAndSubject_IdAndTeacher_Id(
                    groupId,
                    request.subjectId(),
                    request.teacherId()
            );

            if (!isAssigned) {
                throw new InvalidValueException(
                        "Преподаватель не ведет данную дисциплину у группы",
                        "Teacher with id=" + request.teacherId()
                                + " is not assigned to subjectId=" + request.subjectId()
                                + " for groupId=" + groupId
                );
            }

            entity.addGroup(foundedGroup);
        }
    }

    private void validateAudienceCollision(
            Long excludeId,
            ScheduleRequest request
    ) {
        boolean hasCollision = scheduleRepository.hasAudienceCollision(
                request.audience(),
                request.date(),
                request.timeFrom(),
                request.timeTo(),
                excludeId
        );

        if (hasCollision) {
            throw new AlreadyExistsException(
                    "Аудитория занята на это время",
                    "Audience " + request.audience() + " is already occupied on " + request.date()
                            + " between " + request.timeFrom() + " and " + request.timeTo()
            );
        }
    }
}