package com.fraus.spring.universityapi.position.domain;

import com.fraus.spring.universityapi.department.domain.DepartmentRepository;
import com.fraus.spring.universityapi.department.domain.db.DepartmentEntity;
import com.fraus.spring.universityapi.department.web.dto.DepartmentResponse;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.position.domain.db.DepartmentPositionEntity;
import com.fraus.spring.universityapi.position.domain.db.PositionEntity;
import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionRequest;
import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionResponse;
import com.fraus.spring.universityapi.position.web.dto.PositionResponse;
import com.fraus.spring.universityapi.position.web.mapper.DepartmentPositionMapper;
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
@DisplayName("Unit-тестирование DepartmentPositionService")
class DepartmentPositionServiceTest {

    @Mock
    private DepartmentPositionRepository departmentPositionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private DepartmentPositionMapper departmentPositionMapper;

    @InjectMocks
    private DepartmentPositionService departmentPositionService;

    private PositionEntity createTestPositionEntity(Short id) {
        PositionEntity position = spy(new PositionEntity());
        if (id != null) {
            ReflectionTestUtils.setField(position, "id", id);
        }
        position.setName("Доцент");
        ReflectionTestUtils.setField(position, "departmentPositions", new ArrayList<>());
        return position;
    }

    private DepartmentEntity createTestDepartmentEntity(Short id) {
        DepartmentEntity department = spy(new DepartmentEntity());
        if (id != null) {
            ReflectionTestUtils.setField(department, "id", id);
        }
        department.setName("Кафедра ПОИТ");
        ReflectionTestUtils.setField(department, "departmentPositions", new ArrayList<>());
        return department;
    }

    private DepartmentPositionEntity createTestDepartmentPositionEntity(Integer id, DepartmentEntity department, PositionEntity position) {
        DepartmentPositionEntity entity = spy(new DepartmentPositionEntity());
        if (id != null) {
            ReflectionTestUtils.setField(entity, "id", id);
        }
        entity.setDepartment(department);
        entity.setPosition(position);
        return entity;
    }

    private DepartmentPositionRequest createTestRequest(Short positionId, Short departmentId) {
        return new DepartmentPositionRequest(positionId, departmentId);
    }

    private DepartmentPositionResponse createTestResponse(Integer id, Short positionId, Short departmentId) {
        PositionResponse positionResp = new PositionResponse(positionId, "Доцент");
        DepartmentResponse departmentResp = new DepartmentResponse(departmentId, "Кафедра ПОИТ", null);
        return new DepartmentPositionResponse(id,positionResp, departmentResp);
    }

    @Nested
    @DisplayName("Создание должности кафедры")
    class CreateDepartmentPosition {

        @Test
        @DisplayName("Должен успешно привязать должность к кафедре")
        void shouldCreateDepartmentPositionSuccessfully() {
            Short positionId = 1;
            Short departmentId = 2;
            DepartmentPositionRequest request = createTestRequest(positionId, departmentId);

            PositionEntity position = createTestPositionEntity(positionId);
            DepartmentEntity department = createTestDepartmentEntity(departmentId);

            DepartmentPositionEntity savedEntity = createTestDepartmentPositionEntity(10, department, position);
            DepartmentPositionResponse expectedResponse = createTestResponse(10, positionId, departmentId);

            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(departmentPositionRepository.save(any(DepartmentPositionEntity.class))).thenReturn(savedEntity);
            when(departmentPositionMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

            DepartmentPositionResponse actualResponse = departmentPositionService.createDepartmentPositions(request);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(10);

            verify(position, times(1)).addDepartmentPosition(any(DepartmentPositionEntity.class));
            verify(department, times(1)).addDepartmentPosition(any(DepartmentPositionEntity.class));
            verify(departmentPositionRepository, times(1)).save(any(DepartmentPositionEntity.class));
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если должность не найдена при создании")
        void shouldThrowExceptionWhenPositionNotFoundOnCreate() {
            Short positionId = 99;
            Short departmentId = 2;
            DepartmentPositionRequest request = createTestRequest(positionId, departmentId);

            when(positionRepository.findById(positionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentPositionService.createDepartmentPositions(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Должность не найдена");

            verify(departmentPositionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если кафедра не найдена при создании")
        void shouldThrowExceptionWhenDepartmentNotFoundOnCreate() {
            Short positionId = 1;
            Short departmentId = 99;
            DepartmentPositionRequest request = createTestRequest(positionId, departmentId);

            PositionEntity position = createTestPositionEntity(positionId);

            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentPositionService.createDepartmentPositions(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Кафедра не найдена");

            verify(departmentPositionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Получение должностей кафедры")
    class GetDepartmentPositions {

        @Test
        @DisplayName("Должен вернуть должность кафедры по ID")
        void shouldReturnDepartmentPositionById() {
            Integer id = 10;
            DepartmentPositionEntity entity = createTestDepartmentPositionEntity(id, createTestDepartmentEntity((short) 2), createTestPositionEntity((short) 1));
            DepartmentPositionResponse expectedResponse = createTestResponse(id, (short) 1, (short) 2);

            when(departmentPositionRepository.findById(id)).thenReturn(Optional.of(entity));
            when(departmentPositionMapper.toResponse(entity)).thenReturn(expectedResponse);

            DepartmentPositionResponse actualResponse = departmentPositionService.getDepartmentPositionById(id);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если запись не найдена по ID")
        void shouldThrowExceptionWhenNotFoundById() {
            Integer id = 999;
            when(departmentPositionRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentPositionService.getDepartmentPositionById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Должность кафедры не найдена");
        }

        @Test
        @DisplayName("Должен вернуть страницу должностей кафедр по фильтру")
        void shouldReturnPageByFilter() {

            Pageable pageable = PageRequest.of(0, 10);
            Short positionId = 1;
            Short departmentId = 2;

            DepartmentPositionEntity entity = createTestDepartmentPositionEntity(10, createTestDepartmentEntity(departmentId), createTestPositionEntity(positionId));
            Page<DepartmentPositionEntity> entityPage = new PageImpl<>(List.of(entity));

            DepartmentPositionResponse response = createTestResponse(10, positionId, departmentId);
            Page<DepartmentPositionResponse> responsePage = new PageImpl<>(List.of(response));

            when(departmentPositionRepository.findDepartmentPositionsByFilter(pageable, positionId, departmentId)).thenReturn(entityPage);
            when(departmentPositionMapper.toResponsePage(entityPage)).thenReturn(responsePage);

            Page<DepartmentPositionResponse> result = departmentPositionService.getDepartmentPositionByFilter(pageable, positionId, departmentId);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Обновление должности кафедры")
    class UpdateDepartmentPosition {

        @Test
        @DisplayName("Должен успешно обновить и перепривязать должность и кафедру")
        void shouldUpdateAndRebindSuccessfully() {
            Integer id = 10;
            Short oldPositionId = 1;
            Short newPositionId = 11;
            Short oldDepartmentId = 2;
            Short newDepartmentId = 22;

            PositionEntity oldPosition = createTestPositionEntity(oldPositionId);
            PositionEntity newPosition = createTestPositionEntity(newPositionId);

            DepartmentEntity oldDepartment = createTestDepartmentEntity(oldDepartmentId);
            DepartmentEntity newDepartment = createTestDepartmentEntity(newDepartmentId);

            DepartmentPositionEntity existingEntity = createTestDepartmentPositionEntity(id, oldDepartment, oldPosition);

            DepartmentPositionRequest updateRequest = createTestRequest(newPositionId, newDepartmentId);
            DepartmentPositionResponse expectedResponse = createTestResponse(id, newPositionId, newDepartmentId);

            when(departmentPositionRepository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(positionRepository.findById(newPositionId)).thenReturn(Optional.of(newPosition));
            when(departmentRepository.findById(newDepartmentId)).thenReturn(Optional.of(newDepartment));
            when(departmentPositionMapper.toResponse(existingEntity)).thenReturn(expectedResponse);

            DepartmentPositionResponse actualResponse = departmentPositionService.updateDepartmentPositionById(id, updateRequest);

            assertThat(actualResponse).isNotNull();
            verify(oldPosition, times(1)).removeDepartmentPosition(existingEntity);
            verify(oldDepartment, times(1)).removeDepartmentPosition(existingEntity);
            verify(newPosition, times(1)).addDepartmentPosition(existingEntity);
            verify(newDepartment, times(1)).addDepartmentPosition(existingEntity);
        }
    }

    @Nested
    @DisplayName("Удаление должности кафедры")
    class DeleteDepartmentPosition {

        @Test
        @DisplayName("Должен успешно удалить запись, если она существует")
        void shouldDeleteSuccessfully() {
            Integer id = 10;
            when(departmentPositionRepository.existsById(id)).thenReturn(true);

            departmentPositionService.deleteDepartmentPositionById(id);

            verify(departmentPositionRepository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующей записи")
        void shouldThrowExceptionWhenNotFoundOnDelete() {
            Integer id = 999;
            when(departmentPositionRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> departmentPositionService.deleteDepartmentPositionById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Должность кафедры не найдена");

            verify(departmentPositionRepository, never()).deleteById(any());
        }
    }
}