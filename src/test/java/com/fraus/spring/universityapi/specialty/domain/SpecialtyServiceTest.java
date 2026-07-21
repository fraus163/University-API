package com.fraus.spring.universityapi.specialty.domain;

import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyResponse;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyRequest;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyResponse;
import com.fraus.spring.universityapi.specialty.web.mapper.SpecialtyMapper;
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
@DisplayName("Unit-тестирование SpecialtyService")
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private SpecialtyMapper specialtyMapper;

    @InjectMocks
    private SpecialtyService specialtyService;

    private FacultyEntity createTestFacultyEntity(Short id) {
        FacultyEntity faculty = spy(new FacultyEntity());
        if (id != null) {
            ReflectionTestUtils.setField(faculty, "id", id);
        }
        faculty.setNumber((short) 101);
        faculty.setName("ФИТУ");
        ReflectionTestUtils.setField(faculty, "specialties", new ArrayList<>());
        return faculty;
    }

    private FacultyResponse createTestFacultyResponse(Short id) {
        return new FacultyResponse(id, (short) 101, "ФИТУ");
    }

    private SpecialtyRequest createTestRequest(Short facultyId) {
        return new SpecialtyRequest("Инженерия программного обеспечения", DegreeType.BACHELOR, facultyId);
    }

    private SpecialtyEntity createTestSpecialtyEntity(Short id, FacultyEntity faculty) {
        SpecialtyEntity specialty = spy(new SpecialtyEntity());
        if (id != null) {
            ReflectionTestUtils.setField(specialty, "id", id);
        }
        specialty.setName("Инженерия программного обеспечения");
        specialty.setDegree(DegreeType.BACHELOR);
        specialty.setFaculty(faculty);
        return specialty;
    }

    private SpecialtyResponse createTestSpecialtyResponse(Short id, FacultyResponse facultyResponse) {
        return new SpecialtyResponse(id, "Инженерия программного обеспечения", DegreeType.BACHELOR, facultyResponse);
    }

    @Nested
    @DisplayName("Создание специальности")
    class CreateSpecialty {

        @Test
        @DisplayName("Должен успешно создать специальность и связать с факультетом")
        void shouldCreateSpecialtySuccessfully() {
            Short facultyId = 10;
            SpecialtyRequest request = createTestRequest(facultyId);
            FacultyEntity facultyEntity = createTestFacultyEntity(facultyId);
            FacultyResponse facultyResponse = createTestFacultyResponse(facultyId);

            SpecialtyEntity unsavedSpecialty = createTestSpecialtyEntity(null, null);
            SpecialtyEntity savedSpecialty = createTestSpecialtyEntity((short) 1, facultyEntity);
            SpecialtyResponse expectedResponse = createTestSpecialtyResponse((short) 1, facultyResponse);

            when(specialtyMapper.toEntity(request)).thenReturn(unsavedSpecialty);
            when(facultyRepository.findById(facultyId)).thenReturn(Optional.of(facultyEntity));
            when(specialtyRepository.save(unsavedSpecialty)).thenReturn(savedSpecialty);
            when(specialtyMapper.toResponse(savedSpecialty)).thenReturn(expectedResponse);

            SpecialtyResponse actualResponse = specialtyService.createSpecialty(request);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo((short) 1);
            assertThat(actualResponse.faculty()).isNotNull();
            assertThat(actualResponse.faculty().id()).isEqualTo(facultyId);

            verify(facultyEntity, times(1)).addSpecialty(unsavedSpecialty);
            verify(specialtyRepository, times(1)).save(unsavedSpecialty);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если факультет не найден")
        void shouldThrowExceptionWhenFacultyNotFoundOnCreate() {
            Short facultyId = 99;
            SpecialtyRequest request = createTestRequest(facultyId);
            SpecialtyEntity unsavedSpecialty = createTestSpecialtyEntity(null, null);

            when(specialtyMapper.toEntity(request)).thenReturn(unsavedSpecialty);
            when(facultyRepository.findById(facultyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> specialtyService.createSpecialty(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Факультет не найден");

            verify(specialtyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Получение специальностей")
    class GetSpecialties {

        @Test
        @DisplayName("Должен вернуть специальность по ID")
        void shouldReturnSpecialtyById() {
            Short id = 1;
            Short facultyId = 10;
            FacultyEntity facultyEntity = createTestFacultyEntity(facultyId);
            FacultyResponse facultyResponse = createTestFacultyResponse(facultyId);

            SpecialtyEntity specialtyEntity = createTestSpecialtyEntity(id, facultyEntity);
            SpecialtyResponse expectedResponse = createTestSpecialtyResponse(id, facultyResponse);

            when(specialtyRepository.findById(id)).thenReturn(Optional.of(specialtyEntity));
            when(specialtyMapper.toResponse(specialtyEntity)).thenReturn(expectedResponse);

            SpecialtyResponse actualResponse = specialtyService.getSpecialtyById(id);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(id);
            assertThat(actualResponse.faculty().name()).isEqualTo("ФИТУ");
        }

        @Test
        @DisplayName("Должен вернуть страницу специальностей по фильтру")
        void shouldReturnPageOfSpecialtiesByFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Short facultyId = 10;
            FacultyEntity facultyEntity = createTestFacultyEntity(facultyId);
            FacultyResponse facultyResponse = createTestFacultyResponse(facultyId);

            SpecialtyEntity specialtyEntity = createTestSpecialtyEntity((short) 1, facultyEntity);
            Page<SpecialtyEntity> entityPage = new PageImpl<>(List.of(specialtyEntity));

            SpecialtyResponse response = createTestSpecialtyResponse((short) 1, facultyResponse);
            Page<SpecialtyResponse> responsePage = new PageImpl<>(List.of(response));

            when(specialtyRepository.findSpecialtiesByFilter(pageable, facultyId)).thenReturn(entityPage);
            when(specialtyMapper.toResponsePage(entityPage)).thenReturn(responsePage);

            Page<SpecialtyResponse> result = specialtyService.getSpecialtiesByFilter(pageable, facultyId);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).faculty().id()).isEqualTo(facultyId);
        }
    }

    @Nested
    @DisplayName("Обновление специальности")
    class UpdateSpecialty {

        @Test
        @DisplayName("Должен успешно обновить специальность и перепривязать к новому факультету")
        void shouldUpdateSpecialtySuccessfully() {
            Short specialtyId = 1;
            Short oldFacultyId = 10;
            Short newFacultyId = 20;

            FacultyEntity oldFaculty = createTestFacultyEntity(oldFacultyId);
            FacultyEntity newFaculty = createTestFacultyEntity(newFacultyId);
            SpecialtyEntity existingSpecialty = createTestSpecialtyEntity(specialtyId, oldFaculty);

            SpecialtyRequest updateRequest = new SpecialtyRequest("Прикладная информатика", DegreeType.MASTER, newFacultyId);
            SpecialtyResponse expectedResponse = new SpecialtyResponse(
                    specialtyId,
                    "Прикладная информатика",
                    DegreeType.MASTER,
                    createTestFacultyResponse(newFacultyId)
            );

            when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(existingSpecialty));
            when(facultyRepository.findById(newFacultyId)).thenReturn(Optional.of(newFaculty));
            when(specialtyMapper.toResponse(existingSpecialty)).thenReturn(expectedResponse);

            SpecialtyResponse actualResponse = specialtyService.updateSpecialtyById(specialtyId, updateRequest);

            assertThat(actualResponse).isNotNull();
            assertThat(existingSpecialty.getName()).isEqualTo("Прикладная информатика");
            assertThat(existingSpecialty.getDegree()).isEqualTo(DegreeType.MASTER);
            assertThat(actualResponse.faculty().id()).isEqualTo(newFacultyId);

            verify(oldFaculty, times(1)).removeSpecialty(existingSpecialty);
            verify(newFaculty, times(1)).addSpecialty(existingSpecialty);
        }
    }

    @Nested
    @DisplayName("Удаление специальности")
    class DeleteSpecialty {

        @Test
        @DisplayName("Должен успешно удалить специальность, если она существует")
        void shouldDeleteSpecialtySuccessfully() {
            Short id = 1;
            when(specialtyRepository.existsById(id)).thenReturn(true);

            specialtyService.deleteSpecialtyById(id);

            verify(specialtyRepository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующей специальности")
        void shouldThrowExceptionWhenSpecialtyNotFoundOnDelete() {
            Short id = 99;
            when(specialtyRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> specialtyService.deleteSpecialtyById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Специальность не найдена");

            verify(specialtyRepository, never()).deleteById(any());
        }
    }
}