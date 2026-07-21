package com.fraus.spring.universityapi.position.domain;

import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.position.domain.db.PositionEntity;
import com.fraus.spring.universityapi.position.web.dto.PositionRequest;
import com.fraus.spring.universityapi.position.web.dto.PositionResponse;
import com.fraus.spring.universityapi.position.web.mapper.PositionMapper;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тестирование PositionService")
class PositionServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private PositionMapper positionMapper;

    @InjectMocks
    private PositionService positionService;

    private PositionRequest createTestRequest() {
        return new PositionRequest("Доцент");
    }

    private PositionEntity createTestPositionEntity(Short id) {
        PositionEntity position = new PositionEntity();
        if (id != null) {
            ReflectionTestUtils.setField(position, "id", id);
        }
        position.setName("Доцент");
        return position;
    }

    private PositionResponse createTestPositionResponse(Short id) {
        return new PositionResponse(id, "Доцент");
    }

    @Nested
    @DisplayName("Создание должности")
    class CreatePosition {

        @Test
        @DisplayName("Должен успешно создать должность")
        void shouldCreatePositionSuccessfully() {
            PositionRequest request = createTestRequest();
            PositionEntity unsavedPosition = createTestPositionEntity(null);
            PositionEntity savedPosition = createTestPositionEntity((short) 1);
            PositionResponse expectedResponse = createTestPositionResponse((short) 1);

            when(positionMapper.toEntity(request)).thenReturn(unsavedPosition);
            when(positionRepository.save(unsavedPosition)).thenReturn(savedPosition);
            when(positionMapper.toResponse(savedPosition)).thenReturn(expectedResponse);

            PositionResponse actualResponse = positionService.createPosition(request);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo((short) 1);
            assertThat(actualResponse.name()).isEqualTo("Доцент");

            verify(positionRepository, times(1)).save(unsavedPosition);
        }
    }

    @Nested
    @DisplayName("Получение должностей")
    class GetPositions {

        @Test
        @DisplayName("Должен вернуть должность по ID")
        void shouldReturnPositionById() {
            Short id = 1;
            PositionEntity positionEntity = createTestPositionEntity(id);
            PositionResponse expectedResponse = createTestPositionResponse(id);

            when(positionRepository.findById(id)).thenReturn(Optional.of(positionEntity));
            when(positionMapper.toResponse(positionEntity)).thenReturn(expectedResponse);

            PositionResponse actualResponse = positionService.getPositionById(id);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(id);
            assertThat(actualResponse.name()).isEqualTo("Доцент");
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если должность не найдена по ID")
        void shouldThrowExceptionWhenPositionNotFoundById() {
            Short id = 99;
            when(positionRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> positionService.getPositionById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Должность не найдена");
        }

        @Test
        @DisplayName("Должен вернуть страницу всех должностей")
        void shouldReturnPageOfAllPositions() {
            Pageable pageable = PageRequest.of(0, 10);
            PositionEntity positionEntity = createTestPositionEntity((short) 1);
            Page<PositionEntity> entityPage = new PageImpl<>(List.of(positionEntity));

            PositionResponse response = createTestPositionResponse((short) 1);
            Page<PositionResponse> responsePage = new PageImpl<>(List.of(response));

            when(positionRepository.findAll(pageable)).thenReturn(entityPage);
            when(positionMapper.toResponsePage(entityPage)).thenReturn(responsePage);

            Page<PositionResponse> result = positionService.getAllPositions(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("Доцент");
        }
    }

    @Nested
    @DisplayName("Обновление должности")
    class UpdatePosition {

        @Test
        @DisplayName("Должен успешно обновить название должности")
        void shouldUpdatePositionSuccessfully() {
            Short id = 1;
            PositionEntity existingPosition = createTestPositionEntity(id);
            PositionRequest updateRequest = new PositionRequest("Профессор");
            PositionResponse expectedResponse = new PositionResponse(id, "Профессор");

            when(positionRepository.findById(id)).thenReturn(Optional.of(existingPosition));
            when(positionMapper.toResponse(existingPosition)).thenReturn(expectedResponse);

            PositionResponse actualResponse = positionService.updatePositionById(id, updateRequest);

            assertThat(actualResponse).isNotNull();
            assertThat(existingPosition.getName()).isEqualTo("Профессор");
            assertThat(actualResponse.name()).isEqualTo("Профессор");
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при обновлении несуществующей должности")
        void shouldThrowExceptionWhenPositionNotFoundOnUpdate() {
            Short id = 99;
            PositionRequest request = createTestRequest();

            when(positionRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> positionService.updatePositionById(id, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Должность не найдена");
        }
    }

    @Nested
    @DisplayName("Удаление должности")
    class DeletePosition {

        @Test
        @DisplayName("Должен успешно удалить должность, если она существует")
        void shouldDeletePositionSuccessfully() {
            Short id = 1;
            when(positionRepository.existsById(id)).thenReturn(true);

            positionService.deletePositionById(id);

            verify(positionRepository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующей должности")
        void shouldThrowExceptionWhenPositionNotFoundOnDelete() {
            Short id = 99;
            when(positionRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> positionService.deletePositionById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Должность не найдена");

            verify(positionRepository, never()).deleteById(any());
        }
    }
}