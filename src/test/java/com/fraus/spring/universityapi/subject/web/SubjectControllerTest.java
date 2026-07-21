package com.fraus.spring.universityapi.subject.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.subject.domain.SubjectService;
import com.fraus.spring.universityapi.subject.web.dto.SubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.SubjectResponse;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubjectController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование SubjectController")
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private SubjectRequest createValidRequest() {
        return new SubjectRequest("Высшая математика", "Базовый курс математического анализа");
    }

    private SubjectResponse createTestResponse(Integer id) {
        return new SubjectResponse(id, "Высшая математика", "Базовый курс математического анализа");
    }

    @Nested
    @DisplayName("POST /api/v1/subjects - Создание дисциплины")
    class CreateSubject {

        @Test
        @DisplayName("Должен вернуть 201 Created при вызове с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateSubjectAndReturn201ForAdmin() throws Exception {
            SubjectRequest request = createValidRequest();
            SubjectResponse response = createTestResponse(1);

            when(subjectService.createSubject(any(SubjectRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/subjects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/subjects/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Высшая математика"))
                    .andExpect(jsonPath("$.description").value("Базовый курс математического анализа"));

            verify(subjectService, times(1)).createSubject(any(SubjectRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке создания ролями STUDENT или TEACHER")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403WhenCreatedByStudent() throws Exception {
            SubjectRequest request = createValidRequest();

            mockMvc.perform(post("/api/v1/subjects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(subjectService, never()).createSubject(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/subjects - Получение дисциплин")
    class GetSubjects {

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу дисциплин для роли STUDENT")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnPageOfSubjectsForStudent() throws Exception {
            String filterName = "Мат";
            SubjectResponse response = createTestResponse(1);
            Page<SubjectResponse> responsePage = new PageImpl<>(List.of(response));

            when(subjectService.getSubjectsByFilter(any(Pageable.class), eq(filterName))).thenReturn(responsePage);

            mockMvc.perform(get("/api/v1/subjects")
                            .param("name", filterName)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Высшая математика"));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и дисциплину по ID для роли TEACHER")
        @WithMockUser(roles = "TEACHER")
        void shouldReturnSubjectByIdForTeacher() throws Exception {
            Integer id = 1;
            SubjectResponse response = createTestResponse(id);

            when(subjectService.getSubjectById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/subjects/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Высшая математика"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если дисциплина не найдена")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenSubjectNotFound() throws Exception {
            Integer id = 999;
            when(subjectService.getSubjectById(id))
                    .thenThrow(new ResourceNotFoundException("Дисциплина не найдена", "Subject not found: subjectId=" + id));

            mockMvc.perform(get("/api/v1/subjects/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Дисциплина не найдена"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/subjects/{id} - Обновление дисциплины")
    class UpdateSubject {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении с ролью ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateSubjectSuccessfully() throws Exception {
            Integer id = 1;
            SubjectRequest request = createValidRequest();
            SubjectResponse response = createTestResponse(id);

            when(subjectService.updateSubjectById(eq(id), any(SubjectRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/subjects/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Высшая математика"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/subjects/{id} - Удаление дисциплины")
    class DeleteSubject {

        @Test
        @DisplayName("Должен вернуть 204 No Content для роли ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteSubjectAndReturn204ForAdmin() throws Exception {
            Integer id = 1;
            doNothing().when(subjectService).deleteSubjectById(id);

            mockMvc.perform(delete("/api/v1/subjects/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(subjectService, times(1)).deleteSubjectById(id);
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке удаления ролями TEACHER или STUDENT")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn403WhenDeletingByTeacher() throws Exception {
            Integer id = 1;

            mockMvc.perform(delete("/api/v1/subjects/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(subjectService, never()).deleteSubjectById(any());
        }
    }
}