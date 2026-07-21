package com.fraus.spring.universityapi.subject.domain;

import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.domain.GroupRepository;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.subject.domain.db.ControlType;
import com.fraus.spring.universityapi.subject.domain.db.GroupSubjectEntity;
import com.fraus.spring.universityapi.subject.domain.db.SubjectEntity;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectResponse;
import com.fraus.spring.universityapi.subject.web.dto.SubjectResponse;
import com.fraus.spring.universityapi.subject.web.mapper.GroupSubjectMapper;
import com.fraus.spring.universityapi.teacher.domain.TeacherRepository;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тестирование GroupSubjectService")
class GroupSubjectServiceTest {

    @Mock
    private GroupSubjectRepository groupSubjectRepository;

    @Mock
    private GroupSubjectMapper groupSubjectMapper;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private GroupSubjectService groupSubjectService;

    private GroupEntity createTestGroupEntity(Integer id) {
        GroupEntity group = spy(new GroupEntity());
        if (id != null) {
            ReflectionTestUtils.setField(group, "id", id);
        }
        group.setName("ПО-11");
        group.setCourse((short) 1);
        ReflectionTestUtils.setField(group, "groupSubjects", new ArrayList<>());
        return group;
    }

    private SubjectEntity createTestSubjectEntity(Integer id) {
        SubjectEntity subject = spy(new SubjectEntity());
        if (id != null) {
            ReflectionTestUtils.setField(subject, "id", id);
        }
        subject.setName("Высшая математика");
        ReflectionTestUtils.setField(subject, "groupSubjects", new ArrayList<>());
        return subject;
    }

    private TeacherEntity createTestTeacherEntity(Long id) {
        TeacherEntity teacher = spy(new TeacherEntity());
        if (id != null) {
            ReflectionTestUtils.setField(teacher, "id", id);
        }
        ReflectionTestUtils.setField(teacher, "groupSubjects", new ArrayList<>());
        return teacher;
    }

    private GroupSubjectEntity createTestGroupSubjectEntity(Integer id, GroupEntity group, SubjectEntity subject, TeacherEntity teacher) {
        GroupSubjectEntity gs = spy(new GroupSubjectEntity());
        if (id != null) {
            ReflectionTestUtils.setField(gs, "id", id);
        }
        gs.setGroup(group);
        gs.setSubject(subject);
        gs.setTeacher(teacher);
        gs.setTerm((short) 1);
        gs.setHours((short) 120);
        gs.setTypeOfControl(ControlType.EXAM);
        return gs;
    }

    private GroupSubjectRequest createTestRequest(Integer groupId, Integer subjectId, Long teacherId) {
        return new GroupSubjectRequest(groupId, subjectId, teacherId, (short) 1, (short) 120, ControlType.EXAM);
    }

    private GroupSubjectResponse createTestResponse(Integer id, Integer groupId, Integer subjectId, Long teacherId) {
        GroupResponse groupResp = new GroupResponse(groupId, "ПО-11", (short) 1, null);
        SubjectResponse subjectResp = new SubjectResponse(subjectId, "Высшая математика", "Описание");
        TeacherResponse teacherResp = new TeacherResponse(
                teacherId,
                "Иванов",
                "Иван",
                "Иванович",
                "+375291234567",
                "teacher@university.com",
                (short) 5,
                null,
                null,
                List.of()
        );
        return new GroupSubjectResponse(id, groupResp, subjectResp, teacherResp, (short) 1, (short) 120, ControlType.EXAM);
    }

    @Nested
    @DisplayName("Создание назначения дисциплины группе")
    class CreateGroupSubject {

        @Test
        @DisplayName("Должен успешно привязать дисциплину и преподавателя к группе")
        void shouldCreateGroupSubjectSuccessfully() {
            Integer groupId = 1;
            Integer subjectId = 2;
            Long teacherId = 5L;

            GroupSubjectRequest request = createTestRequest(groupId, subjectId, teacherId);
            GroupEntity group = createTestGroupEntity(groupId);
            SubjectEntity subject = createTestSubjectEntity(subjectId);
            TeacherEntity teacher = createTestTeacherEntity(teacherId);

            GroupSubjectEntity unsavedEntity = createTestGroupSubjectEntity(null, null, null, null);
            GroupSubjectEntity savedEntity = createTestGroupSubjectEntity(10, group, subject, teacher);
            GroupSubjectResponse expectedResponse = createTestResponse(10, groupId, subjectId, teacherId);

            when(groupSubjectMapper.toEntity(request)).thenReturn(unsavedEntity);
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
            when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
            when(groupSubjectRepository.save(unsavedEntity)).thenReturn(savedEntity);
            when(groupSubjectMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

            GroupSubjectResponse actualResponse = groupSubjectService.createGroupSubject(request);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(10);
            verify(group, times(1)).addGroupSubject(unsavedEntity);
            verify(subject, times(1)).addGroupSubject(unsavedEntity);
            verify(teacher, times(1)).addGroupSubject(unsavedEntity);
            verify(groupSubjectRepository, times(1)).save(unsavedEntity);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если группа не найдена при создании")
        void shouldThrowExceptionWhenGroupNotFoundOnCreate() {
            Integer groupId = 99;
            Integer subjectId = 2;
            Long teacherId = 5L;
            GroupSubjectRequest request = createTestRequest(groupId, subjectId, teacherId);

            when(groupSubjectMapper.toEntity(request)).thenReturn(new GroupSubjectEntity());
            when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupSubjectService.createGroupSubject(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Группа не найдена");

            verify(groupSubjectRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если дисциплина не найдена при создании")
        void shouldThrowExceptionWhenSubjectNotFoundOnCreate() {
            Integer groupId = 1;
            Integer subjectId = 99;
            Long teacherId = 5L;
            GroupSubjectRequest request = createTestRequest(groupId, subjectId, teacherId);
            GroupEntity group = createTestGroupEntity(groupId);

            when(groupSubjectMapper.toEntity(request)).thenReturn(new GroupSubjectEntity());
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupSubjectService.createGroupSubject(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Дисциплина не найдена");

            verify(groupSubjectRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если преподаватель не найден при создании")
        void shouldThrowExceptionWhenTeacherNotFoundOnCreate() {
            Integer groupId = 1;
            Integer subjectId = 2;
            Long teacherId = 99L;
            GroupSubjectRequest request = createTestRequest(groupId, subjectId, teacherId);

            GroupEntity group = createTestGroupEntity(groupId);
            SubjectEntity subject = createTestSubjectEntity(subjectId);

            when(groupSubjectMapper.toEntity(request)).thenReturn(new GroupSubjectEntity());
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
            when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupSubjectService.createGroupSubject(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Преподаватель не найден");

            verify(groupSubjectRepository, never()).save(any());
        }

        @Nested
        @DisplayName("Получение назначений дисциплин")
        class GetGroupSubjects {

            @Test
            @DisplayName("Должен вернуть назначение по ID")
            void shouldReturnGroupSubjectById() {
                Integer id = 10;
                GroupSubjectEntity entity = createTestGroupSubjectEntity(id, createTestGroupEntity(1), createTestSubjectEntity(2), createTestTeacherEntity(5L));
                GroupSubjectResponse expectedResponse = createTestResponse(id, 1, 2, 5L);

                when(groupSubjectRepository.findById(id)).thenReturn(Optional.of(entity));
                when(groupSubjectMapper.toResponse(entity)).thenReturn(expectedResponse);

                GroupSubjectResponse actualResponse = groupSubjectService.getGroupSubjectById(id);

                assertThat(actualResponse).isNotNull();
                assertThat(actualResponse.id()).isEqualTo(id);
            }

            @Test
            @DisplayName("Должен выбросить ResourceNotFoundException, если назначение не найдено по ID")
            void shouldThrowExceptionWhenGroupSubjectNotFoundById() {
                Integer id = 999;
                when(groupSubjectRepository.findById(id)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> groupSubjectService.getGroupSubjectById(id))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessage("Дисциплина группы не найдена");
            }

            @Test
            @DisplayName("Должен вернуть страницу назначений по фильтру")
            void shouldReturnPageOfGroupSubjectsByFilter() {
                Pageable pageable = PageRequest.of(0, 10);
                Integer groupId = 1;
                Integer subjectId = 2;
                Long teacherId = 5L;
                Short term = 1;

                GroupSubjectEntity entity = createTestGroupSubjectEntity(10, createTestGroupEntity(groupId), createTestSubjectEntity(subjectId), createTestTeacherEntity(teacherId));
                Page<GroupSubjectEntity> entityPage = new PageImpl<>(List.of(entity));

                GroupSubjectResponse response = createTestResponse(10, groupId, subjectId, teacherId);
                Page<GroupSubjectResponse> responsePage = new PageImpl<>(List.of(response));

                when(groupSubjectRepository.findGroupSubjectsByFilter(pageable, groupId, subjectId, teacherId, term)).thenReturn(entityPage);
                when(groupSubjectMapper.toResponsePage(entityPage)).thenReturn(responsePage);

                Page<GroupSubjectResponse> result = groupSubjectService.getGroupSubjectsByFilter(pageable, groupId, subjectId, teacherId, term);

                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).id()).isEqualTo(10);
            }
        }

        @Nested
        @DisplayName("Обновление назначения дисциплины")
        class UpdateGroupSubject {

            @Test
            @DisplayName("Должен обновить параметры без перепривязки, если groupId, subjectId и teacherId не изменились")
            void shouldUpdateWithoutChangingGroupSubjectAndTeacher() {
                Integer id = 10;
                Integer groupId = 1;
                Integer subjectId = 2;
                Long teacherId = 5L;

                GroupEntity group = createTestGroupEntity(groupId);
                SubjectEntity subject = createTestSubjectEntity(subjectId);
                TeacherEntity teacher = createTestTeacherEntity(teacherId);

                GroupSubjectEntity existingEntity = createTestGroupSubjectEntity(id, group, subject, teacher);

                GroupSubjectRequest request = new GroupSubjectRequest(groupId, subjectId, teacherId, (short) 2, (short) 150, ControlType.PASS);
                GroupSubjectResponse expectedResponse = new GroupSubjectResponse(id, null, null, null, (short) 2, (short) 150, ControlType.PASS);

                when(groupSubjectRepository.findById(id)).thenReturn(Optional.of(existingEntity));
                when(groupSubjectMapper.toResponse(existingEntity)).thenReturn(expectedResponse);

                GroupSubjectResponse actualResponse = groupSubjectService.updateGroupSubjectById(id, request);

                assertThat(actualResponse).isNotNull();
                assertThat(existingEntity.getTerm()).isEqualTo((short) 2);
                assertThat(existingEntity.getHours()).isEqualTo((short) 150);
                assertThat(existingEntity.getTypeOfControl()).isEqualTo(ControlType.PASS);

                verify(groupRepository, never()).findById(any());
                verify(subjectRepository, never()).findById(any());
                verify(teacherRepository, never()).findById(any());
            }

            @Test
            @DisplayName("Должен перепривязать к новым группе, дисциплине и преподавателю, если ID изменились")
            void shouldRebindGroupSubjectAndTeacherWhenIdsChanged() {
                Integer id = 10;
                Integer oldGroupId = 1;
                Integer newGroupId = 11;
                Integer oldSubjectId = 2;
                Integer newSubjectId = 22;
                Long oldTeacherId = 5L;
                Long newTeacherId = 55L;

                GroupEntity oldGroup = createTestGroupEntity(oldGroupId);
                GroupEntity newGroup = createTestGroupEntity(newGroupId);

                SubjectEntity oldSubject = createTestSubjectEntity(oldSubjectId);
                SubjectEntity newSubject = createTestSubjectEntity(newSubjectId);

                TeacherEntity oldTeacher = createTestTeacherEntity(oldTeacherId);
                TeacherEntity newTeacher = createTestTeacherEntity(newTeacherId);

                GroupSubjectEntity existingEntity = createTestGroupSubjectEntity(id, oldGroup, oldSubject, oldTeacher);
                oldGroup.getGroupSubjects().add(existingEntity);
                oldSubject.getGroupSubjects().add(existingEntity);
                oldTeacher.getGroupSubjects().add(existingEntity);

                GroupSubjectRequest request = new GroupSubjectRequest(newGroupId, newSubjectId, newTeacherId, (short) 2, (short) 140, ControlType.EXAM);

                when(groupSubjectRepository.findById(id)).thenReturn(Optional.of(existingEntity));
                when(groupRepository.findById(newGroupId)).thenReturn(Optional.of(newGroup));
                when(subjectRepository.findById(newSubjectId)).thenReturn(Optional.of(newSubject));
                when(teacherRepository.findById(newTeacherId)).thenReturn(Optional.of(newTeacher));
                when(groupSubjectMapper.toResponse(existingEntity)).thenReturn(createTestResponse(id, newGroupId, newSubjectId, newTeacherId));

                GroupSubjectResponse actualResponse = groupSubjectService.updateGroupSubjectById(id, request);

                assertThat(actualResponse).isNotNull();
                assertThat(oldGroup.getGroupSubjects()).doesNotContain(existingEntity);
                assertThat(oldSubject.getGroupSubjects()).doesNotContain(existingEntity);
                verify(oldTeacher, times(1)).removeGroupSubject(existingEntity);

                verify(newGroup, times(1)).addGroupSubject(existingEntity);
                verify(newSubject, times(1)).addGroupSubject(existingEntity);
                verify(newTeacher, times(1)).addGroupSubject(existingEntity);
            }
        }

        @Nested
        @DisplayName("Удаление назначения дисциплины")
        class DeleteGroupSubject {

            @Test
            @DisplayName("Должен успешно удалить назначение, если оно существует")
            void shouldDeleteGroupSubjectSuccessfully() {
                Integer id = 10;
                when(groupSubjectRepository.existsById(id)).thenReturn(true);

                groupSubjectService.deleteGroupSubjectById(id);

                verify(groupSubjectRepository, times(1)).deleteById(id);
            }

            @Test
            @DisplayName("Должен выбросить ResourceNotFoundException при удалении несуществующего назначения")
            void shouldThrowExceptionWhenGroupSubjectNotFoundOnDelete() {
                Integer id = 999;
                when(groupSubjectRepository.existsById(id)).thenReturn(false);

                assertThatThrownBy(() -> groupSubjectService.deleteGroupSubjectById(id))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessage("Дисциплина группы не найдена");

                verify(groupSubjectRepository, never()).deleteById(any());
            }
        }
    }
}