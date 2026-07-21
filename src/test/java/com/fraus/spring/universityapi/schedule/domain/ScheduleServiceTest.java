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
import com.fraus.spring.universityapi.subject.domain.db.SubjectEntity;
import com.fraus.spring.universityapi.teacher.domain.TeacherRepository;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тестирование ScheduleService")
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupSubjectRepository groupSubjectRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private ScheduleRequest createValidRequest() {
        return new ScheduleRequest(
                5L,
                2,
                LocalTime.of(8, 30),
                LocalTime.of(10, 0),
                LocalDate.now().plusDays(1),
                "301-1",
                List.of(1)
        );
    }

    private TeacherEntity createTestTeacher(Long id) {
        TeacherEntity teacher = new TeacherEntity();
        ReflectionTestUtils.setField(teacher, "id", id);
        return teacher;
    }

    private SubjectEntity createTestSubject(Integer id) {
        SubjectEntity subject = new SubjectEntity();
        ReflectionTestUtils.setField(subject, "id", id);
        return subject;
    }

    private GroupEntity createTestGroup(Integer id) {
        GroupEntity group = new GroupEntity();
        ReflectionTestUtils.setField(group, "id", id);
        return group;
    }

    @Nested
    @DisplayName("Валидация DTO ScheduleRequest")
    class ScheduleRequestValidation {

        @Test
        @DisplayName("Должен выбросить IllegalArgumentException, если время начала позже или равно времени окончания")
        void shouldThrowExceptionWhenTimeFromIsNotBeforeTimeTo() {
            assertThatThrownBy(() -> new ScheduleRequest(
                    5L,
                    2,
                    LocalTime.of(10, 0), // timeFrom
                    LocalTime.of(8, 30), // timeTo (раньше начала)
                    LocalDate.now().plusDays(1),
                    "301-1",
                    List.of(1)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Время начала занятия должно быть раньше времени окончания");
        }
    }

    @Nested
    @DisplayName("Создание занятия в расписании")
    class CreateSchedule {

        @Test
        @DisplayName("Должен успешно создать расписание при соблюдении всех условий")
        void shouldCreateScheduleSuccessfully() {
            ScheduleRequest request = createValidRequest();
            TeacherEntity teacher = createTestTeacher(5L);
            SubjectEntity subject = createTestSubject(2);
            GroupEntity group = createTestGroup(1);

            ScheduleEntity unsaved = new ScheduleEntity();
            ScheduleEntity saved = new ScheduleEntity();
            ReflectionTestUtils.setField(saved, "id", 100L);

            ScheduleResponse expectedResponse = new ScheduleResponse(
                    100L,
                    null,
                    null,
                    request.timeFrom(),
                    request.timeTo(),
                    request.date(),
                    request.audience(),
                    List.of()
            );

            when(scheduleRepository.hasAudienceCollision("301-1", request.date(), request.timeFrom(), request.timeTo(), null))
                    .thenReturn(false);
            when(teacherRepository.findById(5L)).thenReturn(Optional.of(teacher));
            when(subjectRepository.findById(2)).thenReturn(Optional.of(subject));
            when(groupRepository.findById(1)).thenReturn(Optional.of(group));
            when(groupSubjectRepository.existsByGroup_IdAndSubject_IdAndTeacher_Id(1, 2, 5L))
                    .thenReturn(true);

            when(scheduleMapper.toEntity(request)).thenReturn(unsaved);
            when(scheduleRepository.save(unsaved)).thenReturn(saved);
            when(scheduleMapper.toResponse(saved)).thenReturn(expectedResponse);

            ScheduleResponse response = scheduleService.createSchedule(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(100L);
            verify(scheduleRepository, times(1)).save(unsaved);
        }

        @Test
        @DisplayName("Должен выбросить AlreadyExistsException при накладке аудитории по времени")
        void shouldThrowAlreadyExistsExceptionOnAudienceCollision() {
            ScheduleRequest request = createValidRequest();
            when(scheduleRepository.hasAudienceCollision(any(), any(), any(), any(), eq(null)))
                    .thenReturn(true);

            assertThatThrownBy(() -> scheduleService.createSchedule(request))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessage("Аудитория занята на это время");

            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбросить InvalidValueException, если преподаватель не назначен вести этот предмет у группы")
        void shouldThrowInvalidValueExceptionWhenTeacherNotAssignedToGroupSubject() {
            ScheduleRequest request = createValidRequest();
            TeacherEntity teacher = createTestTeacher(5L);
            SubjectEntity subject = createTestSubject(2);
            GroupEntity group = createTestGroup(1);

            when(scheduleRepository.hasAudienceCollision(any(), any(), any(), any(), eq(null))).thenReturn(false);
            when(teacherRepository.findById(5L)).thenReturn(Optional.of(teacher));
            when(subjectRepository.findById(2)).thenReturn(Optional.of(subject));
            when(groupRepository.findById(1)).thenReturn(Optional.of(group));

            when(groupSubjectRepository.existsByGroup_IdAndSubject_IdAndTeacher_Id(1, 2, 5L)).thenReturn(false);

            when(scheduleMapper.toEntity(request)).thenReturn(new ScheduleEntity());

            assertThatThrownBy(() -> scheduleService.createSchedule(request))
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("Преподаватель не ведет данную дисциплину у группы");

            verify(scheduleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Удаление расписания")
    class DeleteSchedule {

        @Test
        @DisplayName("Должен успешно отвязать группы и удалить расписание по ID")
        void shouldDeleteScheduleSuccessfully() {
            Long scheduleId = 100L;
            ScheduleEntity schedule = new ScheduleEntity();
            ReflectionTestUtils.setField(schedule, "id", scheduleId);

            GroupEntity group = createTestGroup(1);
            schedule.addGroup(group);

            when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

            scheduleService.deleteScheduleById(scheduleId);

            assertThat(schedule.getGroups()).isEmpty();
            verify(scheduleRepository, times(1)).delete(schedule);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующего расписания")
        void shouldThrowNotFoundOnDelete() {
            when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.deleteScheduleById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Расписание не найдено");
        }
    }
}