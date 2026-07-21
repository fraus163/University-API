package com.fraus.spring.universityapi.applicant.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.applicant.domain.ApplicantService;
import com.fraus.spring.universityapi.applicant.web.dto.ApplicantRequest;
import com.fraus.spring.universityapi.applicant.web.dto.ApplicantResponse;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicantController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование ApplicantController")
class ApplicantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicantService applicantService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private SpecialtyResponse createTestSpecialtyResponse(Short id) {
        return new SpecialtyResponse(id, "ПОИТ", DegreeType.BACHELOR, null);
    }

    private ApplicantRequest createValidRequest() {
        return new ApplicantRequest((short) 380, (short) 10);
    }

    private ApplicantResponse createTestResponse(Long userId) {
        return new ApplicantResponse(
                userId,
                "Иванов",
                "Иван",
                "Иванович",
                "+375291234567",
                "ivanov@university.com",
                (short) 350,
                createTestSpecialtyResponse((short) 10)
        );
    }

    @Nested
    @DisplayName("GET /api/v1/applicants - Получение абитуриентов")
    class GetApplicants {

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу абитуриентов для роли COMMISSION")
        @WithMockUser(roles = "COMMISSION")
        void shouldReturnPageOfApplicantsForCommission() throws Exception {
            Short specialtyId = 10;
            Short scores = 350;
            String lastName = "Иванов";
            String firstName = "Иван";
            String patronymic = "Иванович";

            ApplicantResponse response = createTestResponse(100L);
            Page<ApplicantResponse> responsePage = new PageImpl<>(List.of(response));

            when(applicantService.getApplicantsByFilter(
                    any(Pageable.class), eq(specialtyId), eq(scores), eq(lastName), eq(firstName), eq(patronymic)
            )).thenReturn(responsePage);

            mockMvc.perform(get("/api/v1/applicants")
                            .param("specialtyId", specialtyId.toString())
                            .param("scores", scores.toString())
                            .param("lastName", lastName)
                            .param("firstName", firstName)
                            .param("patronymic", patronymic)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(100))
                    .andExpect(jsonPath("$.content[0].lastName").value("Иванов"))
                    .andExpect(jsonPath("$.content[0].email").value("ivanov@university.com"))
                    .andExpect(jsonPath("$.content[0].specialty.id").value(10));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и абитуриента по ID для роли ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnApplicantByIdForAdmin() throws Exception {
            Long userId = 100L;
            ApplicantResponse response = createTestResponse(userId);

            when(applicantService.getApplicantById(userId)).thenReturn(response);

            mockMvc.perform(get("/api/v1/applicants/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.lastName").value("Иванов"))
                    .andExpect(jsonPath("$.scores").value(350));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке чтения ролями STUDENT или TEACHER")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403ForStudentOnGet() throws Exception {
            Long userId = 100L;

            mockMvc.perform(get("/api/v1/applicants/{id}", userId))
                    .andExpect(status().isForbidden());

            verify(applicantService, never()).getApplicantById(any());
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если абитуриент не найден")
        @WithMockUser(roles = "COMMISSION")
        void shouldReturn404WhenApplicantNotFound() throws Exception {
            Long userId = 999L;
            when(applicantService.getApplicantById(userId))
                    .thenThrow(new ResourceNotFoundException("Абитуриент не найден", "Applicant not found: userId=" + userId));

            mockMvc.perform(get("/api/v1/applicants/{id}", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Абитуриент не найден"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/applicants/{id} - Обновление данных абитуриента")
    class UpdateApplicant {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateApplicantSuccessfully() throws Exception {
            Long userId = 100L;
            ApplicantRequest request = createValidRequest();
            ApplicantResponse response = createTestResponse(userId);

            when(applicantService.updateApplicantById(eq(userId), any(ApplicantRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/applicants/{id}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке обновления ролью COMMISSION")
        @WithMockUser(roles = "COMMISSION")
        void shouldReturn403ForCommissionOnUpdate() throws Exception {
            Long userId = 100L;
            ApplicantRequest request = createValidRequest();

            mockMvc.perform(put("/api/v1/applicants/{id}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(applicantService, never()).updateApplicantById(any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/applicants/{id} - Удаление абитуриента")
    class DeleteApplicant {

        @Test
        @DisplayName("Должен вернуть 204 No Content для роли ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteApplicantAndReturn204ForAdmin() throws Exception {
            Long userId = 100L;
            doNothing().when(applicantService).deleteApplicantById(userId);

            mockMvc.perform(delete("/api/v1/applicants/{id}", userId)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(applicantService, times(1)).deleteApplicantById(userId);
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке удаления ролью COMMISSION")
        @WithMockUser(roles = "COMMISSION")
        void shouldReturn403ForCommissionOnDelete() throws Exception {
            Long userId = 100L;

            mockMvc.perform(delete("/api/v1/applicants/{id}", userId)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(applicantService, never()).deleteApplicantById(any());
        }
    }
}