package com.fraus.spring.universityapi.department.domain;

import com.fraus.spring.universityapi.department.domain.db.DepartmentEntity;
import com.fraus.spring.universityapi.department.web.dto.DepartmentRequest;
import com.fraus.spring.universityapi.department.web.dto.DepartmentResponse;
import com.fraus.spring.universityapi.department.web.mapper.DepartmentMapper;
import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
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
@DisplayName("Unit-тестирование DepartmentService")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentService departmentService;

    private FacultyEntity createTestFaculty(Short id) {
        FacultyEntity faculty = new FacultyEntity();
        if (id != null) {
            ReflectionTestUtils.setField(faculty, "id", id);
        }
        faculty.setNumber((short) 101);
        faculty.setName("ФИТУ");
        return faculty;
    }

    private DepartmentRequest createTestRequest(Short facultyId) {
        return new DepartmentRequest(facultyId,"Кафедра ПОИТ");
    }

    private DepartmentEntity createTestDepartment(Short id, FacultyEntity faculty) {
        DepartmentEntity department = new DepartmentEntity();
        if (id != null) {
            ReflectionTestUtils.setField(department, "id", id);
        }
        department.setName("Кафедра ПОИТ");
        department.setFaculty(faculty);
        return department;
    }

    private DepartmentResponse createTestResponse(Short id, Short facultyId) {
        return new DepartmentResponse(id, "Кафедра ПОИТ", facultyId);
    }

    @Nested
    @DisplayName("Создание кафедры")
    class CreateDepartment {

        @Test
        @DisplayName("Должен успешно создать кафедру, если факультет найден")
        void shouldCreateDepartmentSuccessfully() {
            Short facultyId = 10;
            DepartmentRequest request = createTestRequest(facultyId);
            FacultyEntity faculty = createTestFaculty(facultyId);
            DepartmentEntity unsavedDepartment = createTestDepartment(null, null);
            DepartmentEntity savedDepartment = createTestDepartment((short) 1, faculty);
            DepartmentResponse expectedResponse = createTestResponse((short) 1, facultyId);

            when(facultyRepository.findById(facultyId)).thenReturn(Optional.of(faculty));
            when(departmentMapper.toEntity(request)).thenReturn(unsavedDepartment);
            when(departmentRepository.save(unsavedDepartment)).thenReturn(savedDepartment);
            when(departmentMapper.toResponse(savedDepartment)).thenReturn(expectedResponse);

            DepartmentResponse actualResponse = departmentService.createDepartment(request);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo((short) 1);
            assertThat(actualResponse.name()).isEqualTo("Кафедра ПОИТ");
            assertThat(unsavedDepartment.getFaculty()).isEqualTo(faculty);

            verify(departmentRepository, times(1)).save(unsavedDepartment);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если факультет не найден при создании")
        void shouldThrowExceptionWhenFacultyNotFoundOnCreate() {
            Short facultyId = 99;
            DepartmentRequest request = createTestRequest(facultyId);

            when(facultyRepository.findById(facultyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.createDepartment(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Факультет не найден");

            verify(departmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Получение кафедр")
    class GetDepartments {

        @Test
        @DisplayName("Должен вернуть список кафедр по фильтру")
        void shouldReturnDepartmentsByFilter() {
            Short facultyId = 10;
            FacultyEntity faculty = createTestFaculty(facultyId);
            DepartmentEntity department = createTestDepartment((short) 1, faculty);
            DepartmentResponse response = createTestResponse((short) 1, facultyId);

            when(departmentRepository.findDepartmentsByFilter(facultyId)).thenReturn(List.of(department));
            when(departmentMapper.toResponseList(List.of(department))).thenReturn(List.of(response));

            List<DepartmentResponse> result = departmentService.getDepartmentsByFilter(facultyId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).facultyId()).isEqualTo(facultyId);
        }

        @Test
        @DisplayName("Должен вернуть кафедру по существующему ID")
        void shouldReturnDepartmentByIdSuccessfully() {
            Short id = 1;
            DepartmentEntity department = createTestDepartment(id, createTestFaculty((short) 10));
            DepartmentResponse expectedResponse = createTestResponse(id, (short) 10);

            when(departmentRepository.findById(id)).thenReturn(Optional.of(department));
            when(departmentMapper.toResponse(department)).thenReturn(expectedResponse);

            DepartmentResponse actualResponse = departmentService.getDepartmentById(id);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если кафедра не найдена")
        void shouldThrowExceptionWhenDepartmentNotFoundById() {
            Short id = 99;
            when(departmentRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.getDepartmentById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Кафедра не найдена");
        }
    }

    @Nested
    @DisplayName("Обновление кафедры")
    class UpdateDepartment {

        @Test
        @DisplayName("Должен успешно обновить кафедру и перепривязать факультет")
        void shouldUpdateDepartmentSuccessfully() {
            Short departmentId = 1;
            Short oldFacultyId = 10;
            Short newFacultyId = 20;

            DepartmentRequest updateRequest = new DepartmentRequest(newFacultyId, "Новая кафедра");
            DepartmentEntity existingDepartment = createTestDepartment(departmentId, createTestFaculty(oldFacultyId));
            FacultyEntity newFaculty = createTestFaculty(newFacultyId);
            DepartmentResponse expectedResponse = new DepartmentResponse(departmentId, "Новая кафедра", newFacultyId);

            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
            when(facultyRepository.findById(newFacultyId)).thenReturn(Optional.of(newFaculty));
            when(departmentMapper.toResponse(existingDepartment)).thenReturn(expectedResponse);

            DepartmentResponse actualResponse = departmentService.updateDepartmentById(departmentId, updateRequest);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.name()).isEqualTo("Новая кафедра");
            assertThat(existingDepartment.getName()).isEqualTo("Новая кафедра");
            assertThat(existingDepartment.getFaculty()).isEqualTo(newFaculty);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при обновлении, если кафедра не найдена")
        void shouldThrowExceptionWhenDepartmentNotFoundOnUpdate() {
            Short departmentId = 99;
            DepartmentRequest request = createTestRequest((short) 10);

            when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.updateDepartmentById(departmentId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Кафедра не найдена");

            verify(facultyRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при обновлении, если новый факультет не найден")
        void shouldThrowExceptionWhenFacultyNotFoundOnUpdate() {
            Short departmentId = 1;
            Short nonExistentFacultyId = 99;
            DepartmentRequest request = createTestRequest(nonExistentFacultyId);
            DepartmentEntity existingDepartment = createTestDepartment(departmentId, createTestFaculty((short) 10));

            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
            when(facultyRepository.findById(nonExistentFacultyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.updateDepartmentById(departmentId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Факультет не найден");

            verify(departmentMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Удаление кафедры")
    class DeleteDepartment {

        @Test
        @DisplayName("Должен успешно удалить кафедру, если она существует")
        void shouldDeleteDepartmentSuccessfully() {
            Short id = 1;
            when(departmentRepository.existsById(id)).thenReturn(true);
            doNothing().when(departmentRepository).deleteById(id);

            departmentService.deleteDepartmentById(id);

            verify(departmentRepository, times(1)).existsById(id);
            verify(departmentRepository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при удалении несуществующей кафедры")
        void shouldThrowExceptionWhenDeletingNonExistentDepartment() {
            Short id = 99;
            when(departmentRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> departmentService.deleteDepartmentById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Кафедра не найдена");

            verify(departmentRepository, never()).deleteById(id);
        }
    }
}