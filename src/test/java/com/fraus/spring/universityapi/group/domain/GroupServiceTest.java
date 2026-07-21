package com.fraus.spring.universityapi.group.domain;

import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.group.web.dto.GroupRequest;
import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.group.web.mapper.GroupMapper;
import com.fraus.spring.universityapi.specialty.domain.SpecialtyRepository;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyResponse;
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
@DisplayName("Unit-тестирование GroupService")
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private GroupService groupService;

    private SpecialtyEntity createTestSpecialtyEntity(Short id) {
        SpecialtyEntity specialty = spy(new SpecialtyEntity());
        if (id != null) {
            ReflectionTestUtils.setField(specialty, "id", id);
        }
        specialty.setName("ПОИТ");
        specialty.setDegree(DegreeType.BACHELOR);
        ReflectionTestUtils.setField(specialty, "groups", new ArrayList<>());
        return specialty;
    }

    private SpecialtyResponse createTestSpecialtyResponse(Short id) {
        return new SpecialtyResponse(id, "ПОИТ", DegreeType.BACHELOR, null);
    }

    private GroupRequest createTestRequest(Short specialtyId) {
        return new GroupRequest("ПО-11", (short) 1, specialtyId);
    }

    private GroupEntity createTestGroupEntity(Integer id, SpecialtyEntity specialty) {
        GroupEntity group = spy(new GroupEntity());
        if (id != null) {
            ReflectionTestUtils.setField(group, "id", id);
        }
        group.setName("ПО-11");
        group.setCourse((short) 1);
        group.setSpecialty(specialty);
        return group;
    }

    private GroupResponse createTestGroupResponse(Integer id, SpecialtyResponse specialtyResponse) {
        return new GroupResponse(id, "ПО-11", (short) 1, specialtyResponse);
    }

    @Nested
    @DisplayName("Создание группы")
    class CreateGroup {

        @Test
        @DisplayName("Должен успешно создать группу и связать со специальностью")
        void shouldCreateGroupSuccessfully() {
            Short specialtyId = 10;
            GroupRequest request = createTestRequest(specialtyId);
            SpecialtyEntity specialtyEntity = createTestSpecialtyEntity(specialtyId);
            SpecialtyResponse specialtyResponse = createTestSpecialtyResponse(specialtyId);

            GroupEntity unsavedGroup = createTestGroupEntity(null, null);
            GroupEntity savedGroup = createTestGroupEntity(1, specialtyEntity);
            GroupResponse expectedResponse = createTestGroupResponse(1, specialtyResponse);

            when(groupMapper.toEntity(request)).thenReturn(unsavedGroup);
            when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialtyEntity));
            when(groupRepository.save(unsavedGroup)).thenReturn(savedGroup);
            when(groupMapper.toResponse(savedGroup)).thenReturn(expectedResponse);

            GroupResponse actualResponse = groupService.createGroup(request);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(1);
            assertThat(actualResponse.specialty()).isNotNull();
            assertThat(actualResponse.specialty().id()).isEqualTo(specialtyId);

            verify(specialtyEntity, times(1)).addGroup(unsavedGroup);
            verify(groupRepository, times(1)).save(unsavedGroup);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если специальность не найдена при создании")
        void shouldThrowExceptionWhenSpecialtyNotFoundOnCreate() {
            Short specialtyId = 99;
            GroupRequest request = createTestRequest(specialtyId);
            GroupEntity unsavedGroup = createTestGroupEntity(null, null);

            when(groupMapper.toEntity(request)).thenReturn(unsavedGroup);
            when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupService.createGroup(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Специальность не найдена");

            verify(groupRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Получение групп")
    class GetGroups {

        @Test
        @DisplayName("Должен вернуть группу по ID")
        void shouldReturnGroupById() {
            Integer id = 1;
            Short specialtyId = 10;
            SpecialtyEntity specialtyEntity = createTestSpecialtyEntity(specialtyId);
            SpecialtyResponse specialtyResponse = createTestSpecialtyResponse(specialtyId);

            GroupEntity groupEntity = createTestGroupEntity(id, specialtyEntity);
            GroupResponse expectedResponse = createTestGroupResponse(id, specialtyResponse);

            when(groupRepository.findById(id)).thenReturn(Optional.of(groupEntity));
            when(groupMapper.toResponse(groupEntity)).thenReturn(expectedResponse);

            GroupResponse actualResponse = groupService.getGroupById(id);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(id);
            assertThat(actualResponse.name()).isEqualTo("ПО-11");
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если группа не найдена по ID")
        void shouldThrowExceptionWhenGroupNotFoundById() {
            Integer id = 999;
            when(groupRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupService.getGroupById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Группа не найдена");
        }

        @Test
        @DisplayName("Должен вернуть страницу групп по фильтру")
        void shouldReturnPageOfGroupsByFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Short specialtyId = 10;
            Short facultyId = 5;
            Short course = 1;

            SpecialtyEntity specialtyEntity = createTestSpecialtyEntity(specialtyId);
            SpecialtyResponse specialtyResponse = createTestSpecialtyResponse(specialtyId);

            GroupEntity groupEntity = createTestGroupEntity(1, specialtyEntity);
            Page<GroupEntity> entityPage = new PageImpl<>(List.of(groupEntity));

            GroupResponse response = createTestGroupResponse(1, specialtyResponse);
            Page<GroupResponse> responsePage = new PageImpl<>(List.of(response));

            when(groupRepository.findGroupsByFilter(pageable, specialtyId, facultyId, course)).thenReturn(entityPage);
            when(groupMapper.toResponsePage(entityPage)).thenReturn(responsePage);

            Page<GroupResponse> result = groupService.getGroupsByFilter(pageable, specialtyId, facultyId, course);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).specialty().id()).isEqualTo(specialtyId);
        }
    }

    @Nested
    @DisplayName("Обновление группы")
    class UpdateGroup {

        @Test
        @DisplayName("Должен успешно обновить группу и перепривязать к новой специальности")
        void shouldUpdateGroupSuccessfully() {
            Integer groupId = 1;
            Short oldSpecialtyId = 10;
            Short newSpecialtyId = 20;

            SpecialtyEntity oldSpecialty = createTestSpecialtyEntity(oldSpecialtyId);
            SpecialtyEntity newSpecialty = createTestSpecialtyEntity(newSpecialtyId);
            GroupEntity existingGroup = createTestGroupEntity(groupId, oldSpecialty);

            GroupRequest updateRequest = new GroupRequest("ПО-21", (short) 2, newSpecialtyId);
            GroupResponse expectedResponse = new GroupResponse(
                    groupId,
                    "ПО-21",
                    (short) 2,
                    createTestSpecialtyResponse(newSpecialtyId)
            );

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(specialtyRepository.findById(newSpecialtyId)).thenReturn(Optional.of(newSpecialty));
            when(groupMapper.toResponse(existingGroup)).thenReturn(expectedResponse);

            GroupResponse actualResponse = groupService.updateGroupById(groupId, updateRequest);

            assertThat(actualResponse).isNotNull();
            assertThat(existingGroup.getName()).isEqualTo("ПО-21");
            assertThat(existingGroup.getCourse()).isEqualTo((short) 2);
            verify(oldSpecialty, times(1)).removeGroup(existingGroup);
            verify(newSpecialty, times(1)).addGroup(existingGroup);
        }
    }

    @Nested
    @DisplayName("Удаление группы")
    class DeleteGroup {

        @Test
        @DisplayName("Должен успешно удалить группу, если она существует")
        void shouldDeleteGroupSuccessfully() {
            Integer id = 1;
            when(groupRepository.existsById(id)).thenReturn(true);

            groupService.deleteGroupById(id);

            verify(groupRepository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующей группы")
        void shouldThrowExceptionWhenGroupNotFoundOnDelete() {
            Integer id = 999;
            when(groupRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> groupService.deleteGroupById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Группа не найдена");

            verify(groupRepository, never()).deleteById(any());
        }
    }
}