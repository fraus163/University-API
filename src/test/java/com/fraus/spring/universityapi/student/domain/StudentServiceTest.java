package com.fraus.spring.universityapi.student.domain;

import com.fraus.spring.universityapi.auth.domain.UserRepository;
import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.domain.GroupRepository;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
import com.fraus.spring.universityapi.student.web.dto.StudentCreateRequest;
import com.fraus.spring.universityapi.student.web.dto.StudentResponse;
import com.fraus.spring.universityapi.student.web.dto.StudentUpdateRequest;
import com.fraus.spring.universityapi.student.web.mapper.StudentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тестирование StudentService")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentService studentService;

    private UserEntity createTestUser(Long id) {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(UserRole.APPLICANT);
        return user;
    }

    private GroupEntity createTestGroup(Integer id) {
        GroupEntity group = new GroupEntity();
        ReflectionTestUtils.setField(group, "id", id);
        return group;
    }

    private StudentEntity createTestStudent(Long id) {
        StudentEntity student = new StudentEntity();
        ReflectionTestUtils.setField(student, "id", id);
        return student;
    }

    @Nested
    @DisplayName("Создание студента")
    class CreateStudent {

        @Test
        @DisplayName("Должен успешно создать студента и сменить роль пользователя на STUDENT")
        void shouldCreateStudentSuccessfully() {
            Long userId = 10L;
            Integer groupId = 1;
            StudentCreateRequest request = new StudentCreateRequest(userId, groupId);

            UserEntity user = createTestUser(userId);
            GroupEntity group = createTestGroup(groupId);
            StudentEntity unsavedStudent = new StudentEntity();
            StudentEntity savedStudent = createTestStudent(100L);

            StudentResponse expectedResponse = new StudentResponse(
                    100L, "Иванов", "Иван", "Иванович", "+79291234567", "student@university.com", null
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(studentMapper.toEntity(request)).thenReturn(unsavedStudent);
            when(studentRepository.save(unsavedStudent)).thenReturn(savedStudent);
            when(studentMapper.toResponse(savedStudent)).thenReturn(expectedResponse);

            StudentResponse response = studentService.createStudent(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(100L);
            assertThat(user.getRole()).isEqualTo(UserRole.STUDENT);
            assertThat(user.getStudent()).isEqualTo(unsavedStudent);

            verify(studentRepository, times(1)).save(unsavedStudent);
        }

        @Test
        @DisplayName("Должен выбросить InvalidValueException, если пользователь уже зарегистрирован как студент")
        void shouldThrowExceptionWhenUserIsAlreadyStudent() {
            Long userId = 10L;
            StudentCreateRequest request = new StudentCreateRequest(userId, 1);

            UserEntity user = createTestUser(userId);
            user.setStudent(createTestStudent(50L)); // Пользователь уже студент

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> studentService.createStudent(request))
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("Пользователь уже является студентом");

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если пользователь не найден")
        void shouldThrowNotFoundWhenUserDoesNotExist() {
            StudentCreateRequest request = new StudentCreateRequest(999L, 1);
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.createStudent(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Пользователь не найден");

            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Обновление данных студента")
    class UpdateStudent {

        @Test
        @DisplayName("Должен успешно перевести студента из одной группы в другую")
        void shouldTransferStudentToNewGroup() {
            Long studentId = 100L;
            Integer oldGroupId = 1;
            Integer newGroupId = 2;

            StudentUpdateRequest request = new StudentUpdateRequest(newGroupId);

            StudentEntity student = createTestStudent(studentId);
            GroupEntity oldGroup = createTestGroup(oldGroupId);
            GroupEntity newGroup = createTestGroup(newGroupId);

            oldGroup.addStudent(student);

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(groupRepository.findById(newGroupId)).thenReturn(Optional.of(newGroup));

            studentService.updateStudentById(studentId, request);

            assertThat(student.getGroup()).isEqualTo(newGroup);
            assertThat(oldGroup.getStudents()).doesNotContain(student);
            assertThat(newGroup.getStudents()).contains(student);
        }
    }

    @Nested
    @DisplayName("Удаление студента")
    class DeleteStudent {

        @Test
        @DisplayName("Должен успешно отвязать студента от группы и удалить из БД")
        void shouldUnlinkGroupAndDeleteStudent() {
            Long studentId = 100L;
            StudentEntity student = createTestStudent(studentId);
            GroupEntity group = createTestGroup(1);
            group.addStudent(student);

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

            studentService.deleteStudentById(studentId);

            assertThat(group.getStudents()).doesNotContain(student);
            verify(studentRepository, times(1)).delete(student);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующего студента")
        void shouldThrowNotFoundOnDelete() {
            when(studentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.deleteStudentById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Студент не найден");

            verify(studentRepository, never()).delete(any());
        }
    }
}