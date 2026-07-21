package com.fraus.spring.universityapi.teacher.domain;

import com.fraus.spring.universityapi.auth.domain.UserRepository;
import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.position.domain.DepartmentPositionRepository;
import com.fraus.spring.universityapi.position.domain.db.DepartmentPositionEntity;
import com.fraus.spring.universityapi.teacher.domain.db.AcademicDegreeType;
import com.fraus.spring.universityapi.teacher.domain.db.AcademicRankType;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherCreateRequest;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherUpdateRequest;
import com.fraus.spring.universityapi.teacher.web.mapper.TeacherMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тестирование TeacherService")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DepartmentPositionRepository departmentPositionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private TeacherService teacherService;

    private UserEntity createTestUser(Long id) {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(UserRole.APPLICANT);
        return user;
    }

    private DepartmentPositionEntity createTestPosition(Integer id) {
        DepartmentPositionEntity position = new DepartmentPositionEntity();
        ReflectionTestUtils.setField(position, "id", id);
        return position;
    }

    private TeacherEntity createTestTeacher(Long id) {
        TeacherEntity teacher = new TeacherEntity();
        ReflectionTestUtils.setField(teacher, "id", id);
        return teacher;
    }

    @Nested
    @DisplayName("Создание преподавателя")
    class CreateTeacher {

        @Test
        @DisplayName("Должен успешно создать преподавателя, обновить роль пользователя и установить должности")
        void shouldCreateTeacherSuccessfully() {
            Long userId = 10L;
            List<Integer> positionIds = List.of(1);
            TeacherCreateRequest request = new TeacherCreateRequest(
                    userId,
                    (short) 5,
                    AcademicRankType.ASSOCIATE_PROFESSOR, // 👈 Изменено на ASSOCIATE_PROFESSOR
                    AcademicDegreeType.CANDIDATE_OF_SCIENCES,
                    positionIds
            );

            UserEntity user = createTestUser(userId);
            DepartmentPositionEntity position = createTestPosition(1);
            TeacherEntity unsavedTeacher = new TeacherEntity();
            TeacherEntity savedTeacher = createTestTeacher(100L);

            TeacherResponse expectedResponse = new TeacherResponse(
                    100L, "Сидоров", "Сидор", "Сидорович", "+79291234567",
                    "teacher@university.com", (short) 5, AcademicRankType.ASSOCIATE_PROFESSOR,
                    AcademicDegreeType.CANDIDATE_OF_SCIENCES, List.of()
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(departmentPositionRepository.findById(1)).thenReturn(Optional.of(position));
            when(teacherMapper.toEntity(request)).thenReturn(unsavedTeacher);
            when(teacherRepository.save(unsavedTeacher)).thenReturn(savedTeacher);
            when(teacherMapper.toResponse(savedTeacher)).thenReturn(expectedResponse);

            TeacherResponse response = teacherService.createTeacher(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(100L);
            assertThat(user.getRole()).isEqualTo(UserRole.TEACHER);
            assertThat(user.getTeacher()).isEqualTo(unsavedTeacher);

            verify(teacherRepository, times(1)).save(unsavedTeacher);
        }

        @Test
        @DisplayName("Должен выбросить InvalidValueException, если пользователь уже зарегистрирован как преподаватель")
        void shouldThrowExceptionWhenUserIsAlreadyTeacher() {
            Long userId = 10L;
            TeacherCreateRequest request = new TeacherCreateRequest(
                    userId, (short) 2, null, null, List.of(1)
            );

            UserEntity user = createTestUser(userId);
            user.setTeacher(createTestTeacher(50L));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> teacherService.createTeacher(request))
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("Пользователь уже является преподавателем");

            verify(teacherRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если должность кафедры не найдена")
        void shouldThrowNotFoundWhenPositionDoesNotExist() {
            Long userId = 10L;
            TeacherCreateRequest request = new TeacherCreateRequest(
                    userId, (short) 2, null, null, List.of(999)
            );

            UserEntity user = createTestUser(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(departmentPositionRepository.findById(999)).thenReturn(Optional.empty());
            when(teacherMapper.toEntity(request)).thenReturn(new TeacherEntity());

            assertThatThrownBy(() -> teacherService.createTeacher(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Должность кафедры не найдена");

            verify(teacherRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Обновление преподавателя")
    class UpdateTeacher {

        @Test
        @DisplayName("Должен переустановить должности кафедры и обновить атрибуты преподавателя")
        void shouldUpdateTeacherSuccessfully() {
            Long teacherId = 100L;
            TeacherUpdateRequest request = new TeacherUpdateRequest(
                    (short) 10,
                    AcademicRankType.PROFESSOR,
                    AcademicDegreeType.DOCTOR_OF_SCIENCES,
                    List.of(2)
            );

            TeacherEntity teacher = createTestTeacher(teacherId);
            DepartmentPositionEntity oldPos = createTestPosition(1);
            DepartmentPositionEntity newPos = createTestPosition(2);

            teacher.addPosition(oldPos);

            when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
            when(departmentPositionRepository.findById(2)).thenReturn(Optional.of(newPos));

            teacherService.updateTeacherById(teacherId, request);

            assertThat(teacher.getAcademicRank()).isEqualTo(AcademicRankType.PROFESSOR);
            assertThat(teacher.getAcademicDegree()).isEqualTo(AcademicDegreeType.DOCTOR_OF_SCIENCES);
            assertThat(teacher.getExperience()).isEqualTo((short) 10);
            assertThat(teacher.getPositions()).containsOnly(newPos);
        }
    }

    @Nested
    @DisplayName("Удаление преподавателя")
    class DeleteTeacher {

        @Test
        @DisplayName("Должен очистить должности и удалить преподавателя")
        void shouldClearPositionsAndDeleteTeacher() {
            Long teacherId = 100L;
            TeacherEntity teacher = createTestTeacher(teacherId);
            teacher.addPosition(createTestPosition(1));

            when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

            teacherService.deleteTeacherById(teacherId);

            assertThat(teacher.getPositions()).isEmpty();
            verify(teacherRepository, times(1)).delete(teacher);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующего преподавателя")
        void shouldThrowNotFoundOnDelete() {
            when(teacherRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teacherService.deleteTeacherById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Преподаватель не найден");

            verify(teacherRepository, never()).delete(any());
        }
    }
}