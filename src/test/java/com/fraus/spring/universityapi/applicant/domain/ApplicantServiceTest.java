package com.fraus.spring.universityapi.applicant.domain;

import com.fraus.spring.universityapi.applicant.domain.db.ApplicantEntity;
import com.fraus.spring.universityapi.applicant.web.dto.ApplicantRequest;
import com.fraus.spring.universityapi.applicant.web.dto.ApplicantResponse;
import com.fraus.spring.universityapi.applicant.web.mapper.ApplicantMapper;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
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
@DisplayName("Unit-тестирование ApplicantService")
class ApplicantServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private ApplicantMapper applicantMapper;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private ApplicantService applicantService;

    private SpecialtyEntity createTestSpecialtyEntity(Short id) {
        SpecialtyEntity specialty = spy(new SpecialtyEntity());
        if (id != null) {
            ReflectionTestUtils.setField(specialty, "id", id);
        }
        specialty.setName("ПОИТ");
        specialty.setDegree(DegreeType.BACHELOR);
        ReflectionTestUtils.setField(specialty, "applicants", new ArrayList<>());
        return specialty;
    }

    private SpecialtyResponse createTestSpecialtyResponse(Short id) {
        return new SpecialtyResponse(id, "ПОИТ", DegreeType.BACHELOR, null);
    }

    private ApplicantEntity createTestApplicantEntity(Long userId, SpecialtyEntity specialty) {
        ApplicantEntity applicant = spy(new ApplicantEntity());
        if (userId != null) {
            ReflectionTestUtils.setField(applicant, "id", userId);
        }
        applicant.setScores((short) 350);
        applicant.setSpecialty(specialty);
        return applicant;
    }

    private ApplicantRequest createTestRequest(Short specialtyId) {
        return new ApplicantRequest((short) 380, specialtyId);
    }

    private ApplicantResponse createTestApplicantResponse(Long userId, SpecialtyResponse specialtyResponse) {
        return new ApplicantResponse(
                userId,
                "Иванов",
                "Иван",
                "Иванович",
                "+375291234567",
                "ivanov@university.com",
                (short) 350,
                specialtyResponse
        );
    }

    @Nested
    @DisplayName("Получение абитуриентов")
    class GetApplicants {

        @Test
        @DisplayName("Должен вернуть абитуриента по userId")
        void shouldReturnApplicantByUserId() {
            Long userId = 100L;
            Short specialtyId = 10;
            SpecialtyEntity specialty = createTestSpecialtyEntity(specialtyId);
            ApplicantEntity applicant = createTestApplicantEntity(userId, specialty);

            SpecialtyResponse specialtyResponse = createTestSpecialtyResponse(specialtyId);
            ApplicantResponse expectedResponse = createTestApplicantResponse(userId, specialtyResponse);

            when(applicantRepository.findById(userId)).thenReturn(Optional.of(applicant));
            when(applicantMapper.toResponse(applicant)).thenReturn(expectedResponse);

            ApplicantResponse actualResponse = applicantService.getApplicantById(userId);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(userId);
            assertThat(actualResponse.scores()).isEqualTo((short) 350);
            assertThat(actualResponse.specialty().id()).isEqualTo(specialtyId);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если абитуриент не найден по userId")
        void shouldThrowExceptionWhenApplicantNotFoundById() {
            Long userId = 999L;
            when(applicantRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicantService.getApplicantById(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Абитуриент не найден");
        }

        @Test
        @DisplayName("Должен вернуть страницу абитуриентов по фильтру")
        void shouldReturnPageOfApplicantsByFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Short specialtyId = 10;
            Short scores = 300;
            String lastName = "Иванов";
            String firstName = "Иван";
            String patronymic = "Иванович";

            ApplicantEntity applicant = createTestApplicantEntity(100L, createTestSpecialtyEntity(specialtyId));
            Page<ApplicantEntity> entityPage = new PageImpl<>(List.of(applicant));

            ApplicantResponse response = createTestApplicantResponse(100L, createTestSpecialtyResponse(specialtyId));
            Page<ApplicantResponse> responsePage = new PageImpl<>(List.of(response));

            when(applicantRepository.findApplicantsByFilter(pageable, specialtyId, scores, lastName, firstName, patronymic))
                    .thenReturn(entityPage);
            when(applicantMapper.toResponsePage(entityPage)).thenReturn(responsePage);

            Page<ApplicantResponse> result = applicantService.getApplicantsByFilter(
                    pageable, specialtyId, scores, lastName, firstName, patronymic
            );

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("Обновление данных абитуриента")
    class UpdateApplicant {

        @Test
        @DisplayName("Должен успешно обновить баллы и перепривязать к новой специальности")
        void shouldUpdateApplicantSuccessfully() {
            Long userId = 100L;
            Short oldSpecialtyId = 10;
            Short newSpecialtyId = 20;

            SpecialtyEntity oldSpecialty = createTestSpecialtyEntity(oldSpecialtyId);
            SpecialtyEntity newSpecialty = createTestSpecialtyEntity(newSpecialtyId);

            ApplicantEntity existingApplicant = createTestApplicantEntity(userId, oldSpecialty);
            ApplicantRequest updateRequest = createTestRequest(newSpecialtyId);

            ApplicantResponse expectedResponse = new ApplicantResponse(
                    userId,
                    "Иванов",
                    "Иван",
                    "Иванович",
                    "+375291234567",
                    "ivanov@university.com",
                    (short) 380,
                    createTestSpecialtyResponse(newSpecialtyId)
            );

            when(applicantRepository.findById(userId)).thenReturn(Optional.of(existingApplicant));
            when(specialtyRepository.findById(newSpecialtyId)).thenReturn(Optional.of(newSpecialty));
            when(applicantMapper.toResponse(existingApplicant)).thenReturn(expectedResponse);

            ApplicantResponse actualResponse = applicantService.updateApplicantById(userId, updateRequest);

            assertThat(actualResponse).isNotNull();
            assertThat(existingApplicant.getScores()).isEqualTo((short) 380);

            verify(oldSpecialty, times(1)).removeApplicant(existingApplicant);
            verify(newSpecialty, times(1)).addApplicant(existingApplicant);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке обновления несуществующего абитуриента")
        void shouldThrowExceptionWhenApplicantNotFoundOnUpdate() {
            Long userId = 999L;
            ApplicantRequest request = createTestRequest((short) 10);

            when(applicantRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicantService.updateApplicantById(userId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Абитуриент не найден");

            verify(specialtyRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException, если указываемая специальность не найдена при обновлении")
        void shouldThrowExceptionWhenSpecialtyNotFoundOnUpdate() {
            Long userId = 100L;
            Short specialtyId = 99;
            ApplicantEntity existingApplicant = createTestApplicantEntity(userId, null);
            ApplicantRequest request = createTestRequest(specialtyId);

            when(applicantRepository.findById(userId)).thenReturn(Optional.of(existingApplicant));
            when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicantService.updateApplicantById(userId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Специальность не найдена");
        }
    }

    @Nested
    @DisplayName("Удаление абитуриента")
    class DeleteApplicant {

        @Test
        @DisplayName("Должен успешно удалить абитуриента, если он существует")
        void shouldDeleteApplicantSuccessfully() {
            Long userId = 100L;
            when(applicantRepository.existsById(userId)).thenReturn(true);

            applicantService.deleteApplicantById(userId);

            verify(applicantRepository, times(1)).deleteById(userId);
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующего абитуриента")
        void shouldThrowExceptionWhenApplicantNotFoundOnDelete() {
            Long userId = 999L;
            when(applicantRepository.existsById(userId)).thenReturn(false);

            assertThatThrownBy(() -> applicantService.deleteApplicantById(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Абитуриент не найден");

            verify(applicantRepository, never()).deleteById(any());
        }
    }
}