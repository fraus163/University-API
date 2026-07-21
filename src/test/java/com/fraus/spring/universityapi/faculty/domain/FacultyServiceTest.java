package com.fraus.spring.universityapi.faculty.domain;

import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyRequest;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyResponse;
import com.fraus.spring.universityapi.faculty.web.mapper.FacultyMapper;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тестирование FacultyService")
class FacultyServiceTest {

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private FacultyMapper facultyMapper;

    @InjectMocks
    private FacultyService facultyService;

    private FacultyRequest createTestRequest() {
        return new FacultyRequest((short) 101, "Информационные технологии");
    }

    private FacultyEntity createTestEntity(Short id) {
        FacultyEntity entity = new FacultyEntity();
        entity.setNumber((short) 101);
        entity.setName("Информационные технологии");

        if (id != null) {
            ReflectionTestUtils.setField(entity, "id", id);
        }

        return entity;
    }

    private FacultyResponse createTestResponse(Short id) {
        return new FacultyResponse(id, (short) 101, "Информационные технологии");
    }

    @Nested
    @DisplayName("Создание факультета")
    class CreateFaculty {

        @Test
        @DisplayName("Должен успешно создать факультет и вернуть ответ")
        void shouldCreateFacultySuccessfully() {
            FacultyRequest request = createTestRequest();
            FacultyEntity unsavedEntity = createTestEntity(null);
            FacultyEntity savedEntity = createTestEntity((short) 1);
            FacultyResponse expectedResponse = createTestResponse((short) 1);

            when(facultyMapper.toEntity(request)).thenReturn(unsavedEntity);
            when(facultyRepository.save(unsavedEntity)).thenReturn(savedEntity);
            when(facultyMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

            FacultyResponse actualResponse = facultyService.createFaculty(request);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo((short) 1);
            assertThat(actualResponse.name()).isEqualTo(request.name());

            verify(facultyRepository, times(1)).save(unsavedEntity);
        }
    }

    @Nested
    @DisplayName("Получение списков и одиночных факультетов")
    class GetFaculties {

        @Test
        @DisplayName("Должен вернуть список всех факультетов")
        void shouldReturnListOfFaculties() {
            FacultyEntity entity = createTestEntity((short) 1);
            FacultyResponse response = createTestResponse((short) 1);

            when(facultyRepository.findAll()).thenReturn(List.of(entity));
            when(facultyMapper.toResponseList(anyList())).thenReturn(List.of(response));

            List<FacultyResponse> result = facultyService.getAllFaculties();

            assertThat(result).isNotEmpty().hasSize(1);
            assertThat(result.get(0).id()).isEqualTo((short) 1);
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если факультетов нет")
        void shouldReturnEmptyListWhenNoFaculties() {
            when(facultyRepository.findAll()).thenReturn(Collections.emptyList());
            when(facultyMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());

            List<FacultyResponse> result = facultyService.getAllFaculties();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Должен успешно вернуть факультет по существующему ID")
        void shouldReturnFacultyByIdSuccessfully() {
            Short id = 1;
            FacultyEntity entity = createTestEntity(id);
            FacultyResponse expectedResponse = createTestResponse(id);

            when(facultyRepository.findById(id)).thenReturn(Optional.of(entity));
            when(facultyMapper.toResponse(entity)).thenReturn(expectedResponse);

            FacultyResponse actualResponse = facultyService.getFacultyById(id);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если факультет по ID не найден")
        void shouldThrowExceptionWhenFacultyNotFoundById() {
            Short id = 99;
            when(facultyRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> facultyService.getFacultyById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Факультет не найден");
        }
    }

    @Nested
    @DisplayName("Обновление факультета")
    class UpdateFaculty {

        @Test
        @DisplayName("Должен успешно обновить существующий факультет")
        void shouldUpdateFacultySuccessfully() {
            Short id = 1;
            FacultyRequest updateRequest = new FacultyRequest((short) 102, "Новое название");
            FacultyEntity existingFaculty = createTestEntity(id);
            FacultyResponse expectedResponse = new FacultyResponse(id, (short) 102, "Новое название");

            when(facultyRepository.findById(id)).thenReturn(Optional.of(existingFaculty));
            when(facultyMapper.toResponse(existingFaculty)).thenReturn(expectedResponse);

            FacultyResponse actualResponse = facultyService.updateFacultyById(id, updateRequest);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.name()).isEqualTo("Новое название");
            assertThat(existingFaculty.getNumber()).isEqualTo((short) 102);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке обновить несуществующий факультет")
        void shouldThrowExceptionWhenUpdatingNonExistentFaculty() {
            Short id = 99;
            FacultyRequest request = createTestRequest();
            when(facultyRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> facultyService.updateFacultyById(id, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(facultyMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Удаление факультета")
    class DeleteFaculty {

        @Test
        @DisplayName("Должен успешно удалить факультет, если он существует")
        void shouldDeleteFacultySuccessfully() {
            Short id = 1;
            when(facultyRepository.existsById(id)).thenReturn(true);
            doNothing().when(facultyRepository).deleteById(id);

            facultyService.deleteFacultyById(id);

            verify(facultyRepository, times(1)).existsById(id);
            verify(facultyRepository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при удалении несуществующего факультета")
        void shouldThrowExceptionWhenDeletingNonExistentFaculty() {
            Short id = 99;
            when(facultyRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> facultyService.deleteFacultyById(id))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(facultyRepository, never()).deleteById(id);
        }
    }
}
