package com.fraus.spring.universityapi.subject.domain;

import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.subject.domain.db.SubjectEntity;
import com.fraus.spring.universityapi.subject.web.dto.SubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.SubjectResponse;
import com.fraus.spring.universityapi.subject.web.mapper.SubjectMapper;
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
@DisplayName("Unit-тестирование SubjectService")
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectMapper subjectMapper;

    @InjectMocks
    private SubjectService subjectService;

    private SubjectRequest createTestRequest() {
        return new SubjectRequest("Высшая математика", "Базовый курс математического анализа");
    }

    private SubjectEntity createTestSubjectEntity(Integer id) {
        SubjectEntity subject = new SubjectEntity();
        if (id != null) {
            ReflectionTestUtils.setField(subject, "id", id);
        }
        subject.setName("Высшая математика");
        subject.setDescription("Базовый курс математического анализа");
        return subject;
    }

    private SubjectResponse createTestSubjectResponse(Integer id) {
        return new SubjectResponse(id, "Высшая математика", "Базовый курс математического анализа");
    }

    @Nested
    @DisplayName("Создание дисциплины")
    class CreateSubject {

        @Test
        @DisplayName("Должен успешно создать дисциплину")
        void shouldCreateSubjectSuccessfully() {
            SubjectRequest request = createTestRequest();
            SubjectEntity unsavedSubject = createTestSubjectEntity(null);
            SubjectEntity savedSubject = createTestSubjectEntity(1);
            SubjectResponse expectedResponse = createTestSubjectResponse(1);

            when(subjectMapper.toEntity(request)).thenReturn(unsavedSubject);
            when(subjectRepository.save(unsavedSubject)).thenReturn(savedSubject);
            when(subjectMapper.toResponse(savedSubject)).thenReturn(expectedResponse);

            SubjectResponse actualResponse = subjectService.createSubject(request);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(1);
            assertThat(actualResponse.name()).isEqualTo("Высшая математика");

            verify(subjectRepository, times(1)).save(unsavedSubject);
        }
    }

    @Nested
    @DisplayName("Получение дисциплин")
    class GetSubjects {

        @Test
        @DisplayName("Должен вернуть дисциплину по ID")
        void shouldReturnSubjectById() {
            Integer id = 1;
            SubjectEntity subjectEntity = createTestSubjectEntity(id);
            SubjectResponse expectedResponse = createTestSubjectResponse(id);

            when(subjectRepository.findById(id)).thenReturn(Optional.of(subjectEntity));
            when(subjectMapper.toResponse(subjectEntity)).thenReturn(expectedResponse);

            SubjectResponse actualResponse = subjectService.getSubjectById(id);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(id);
            assertThat(actualResponse.name()).isEqualTo("Высшая математика");
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если дисциплина не найдена по ID")
        void shouldThrowExceptionWhenSubjectNotFoundById() {
            Integer id = 999;
            when(subjectRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subjectService.getSubjectById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Дисциплина не найдена");
        }

        @Test
        @DisplayName("Должен вернуть страницу дисциплин по фильтру")
        void shouldReturnPageOfSubjectsByFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            String filterName = "Мат";

            SubjectEntity subjectEntity = createTestSubjectEntity(1);
            Page<SubjectEntity> entityPage = new PageImpl<>(List.of(subjectEntity));

            SubjectResponse response = createTestSubjectResponse(1);
            Page<SubjectResponse> responsePage = new PageImpl<>(List.of(response));

            when(subjectRepository.findSubjectsByFilter(pageable, filterName)).thenReturn(entityPage);
            when(subjectMapper.toResponsePage(entityPage)).thenReturn(responsePage);

            Page<SubjectResponse> result = subjectService.getSubjectsByFilter(pageable, filterName);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("Высшая математика");
        }
    }

    @Nested
    @DisplayName("Обновление дисциплины")
    class UpdateSubject {

        @Test
        @DisplayName("Должен успешно обновить данные дисциплины")
        void shouldUpdateSubjectSuccessfully() {
            Integer id = 1;
            SubjectEntity existingSubject = createTestSubjectEntity(id);
            SubjectRequest updateRequest = new SubjectRequest("Дискретная математика", "Курс теории графов и логики");
            SubjectResponse expectedResponse = new SubjectResponse(id, "Дискретная математика", "Курс теории графов и логики");

            when(subjectRepository.findById(id)).thenReturn(Optional.of(existingSubject));
            when(subjectMapper.toResponse(existingSubject)).thenReturn(expectedResponse);

            SubjectResponse actualResponse = subjectService.updateSubjectById(id, updateRequest);

            assertThat(actualResponse).isNotNull();
            assertThat(existingSubject.getName()).isEqualTo("Дискретная математика");
            assertThat(existingSubject.getDescription()).isEqualTo("Курс теории графов и логики");
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке обновить несуществующую дисциплину")
        void shouldThrowExceptionWhenSubjectNotFoundOnUpdate() {
            Integer id = 999;
            SubjectRequest request = createTestRequest();

            when(subjectRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subjectService.updateSubjectById(id, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Дисциплина не найдена");
        }
    }

    @Nested
    @DisplayName("Удаление дисциплины")
    class DeleteSubject {

        @Test
        @DisplayName("Должен успешно удалить дисциплину, если она существует")
        void shouldDeleteSubjectSuccessfully() {
            Integer id = 1;
            when(subjectRepository.existsById(id)).thenReturn(true);

            subjectService.deleteSubjectById(id);

            verify(subjectRepository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующей дисциплины")
        void shouldThrowExceptionWhenSubjectNotFoundOnDelete() {
            Integer id = 999;
            when(subjectRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> subjectService.deleteSubjectById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Дисциплина не найдена");

            verify(subjectRepository, never()).deleteById(any());
        }
    }
}