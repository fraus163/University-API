package com.fraus.spring.universityapi.scoresheet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.scoresheet.domain.ScoreSheetService;
import com.fraus.spring.universityapi.scoresheet.domain.db.AssessmentType;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetCreateRequest;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetResponse;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetUpdateRequest;
import com.fraus.spring.universityapi.student.web.dto.StudentResponse;
import com.fraus.spring.universityapi.subject.domain.db.ControlType;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectResponse;
import com.fraus.spring.universityapi.subject.web.dto.SubjectResponse;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;
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
import org.springframework.security.access.AccessDeniedException;
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

@WebMvcTest(ScoreSheetController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование ScoreSheetController")
class ScoreSheetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScoreSheetService scoreSheetService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private ScoreSheetCreateRequest createValidCreateRequest() {
        return new ScoreSheetCreateRequest(100L, 10, AssessmentType.EXCELLENT);
    }

    private ScoreSheetUpdateRequest createValidUpdateRequest() {
        return new ScoreSheetUpdateRequest(AssessmentType.GOOD);
    }

    private ScoreSheetResponse createTestResponse(Long id) {
        StudentResponse studentResp = new StudentResponse(
                100L,
                "Иванов",
                "Иван",
                "Иванович",
                "+375291234567",
                "student@university.com",
                null
        );
        GroupResponse groupResp = new GroupResponse(1, "ПО-11", (short) 1, null);
        SubjectResponse subjectResp = new SubjectResponse(2, "Высшая математика", "Описание");
        TeacherResponse teacherResp = new TeacherResponse(
                5L, "Петров", "Пётр", "Петрович", "+375297654321", "teacher@university.com", (short) 10, null, null, List.of()
        );
        GroupSubjectResponse groupSubjectResp = new GroupSubjectResponse(10, groupResp, subjectResp, teacherResp, (short) 1, (short) 120, ControlType.EXAM);

        return new ScoreSheetResponse(id, studentResp, groupSubjectResp, AssessmentType.EXCELLENT);
    }

    @Nested
    @DisplayName("POST /api/v1/score_sheets - Создание оценки в ведомости")
    class CreateScoreSheet {

        @Test
        @DisplayName("Должен вернуть 201 Created для преподавателя")
        @WithMockUser(roles = "TEACHER")
        void shouldCreateScoreSheetForTeacher() throws Exception {
            ScoreSheetCreateRequest request = createValidCreateRequest();
            ScoreSheetResponse response = createTestResponse(1L);

            when(scoreSheetService.createScoreSheet(any(ScoreSheetCreateRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/score_sheets")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/score_sheets/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.assessment").value("EXCELLENT"))
                    .andExpect(jsonPath("$.student.id").value(100))
                    .andExpect(jsonPath("$.subject.id").value(10));

            verify(scoreSheetService, times(1)).createScoreSheet(any(ScoreSheetCreateRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden для студента при попытке создания оценки")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403ForStudentOnCreate() throws Exception {
            ScoreSheetCreateRequest request = createValidCreateRequest();

            mockMvc.perform(post("/api/v1/score_sheets")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(scoreSheetService, never()).createScoreSheet(any());
        }

        @Test
        @DisplayName("Должен вернуть 400 Bad Request при передаче невалидного значения оценки (например PASSED для EXAM)")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn400OnInvalidAssessmentValue() throws Exception {
            ScoreSheetCreateRequest request = createValidCreateRequest();
            when(scoreSheetService.createScoreSheet(any(ScoreSheetCreateRequest.class)))
                    .thenThrow(new InvalidValueException("Неверное значение оценки", "Invalid assessment type: PASSED for EXAM"));

            mockMvc.perform(post("/api/v1/score_sheets")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Неверное значение оценки"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/score_sheets - Получение оценок")
    class GetScoreSheets {

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу оценок для роли STUDENT")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnPageForStudent() throws Exception {
            Long studentId = 100L;
            ScoreSheetResponse response = createTestResponse(1L);
            Page<ScoreSheetResponse> page = new PageImpl<>(List.of(response));

            when(scoreSheetService.getScoreSheetsByFilter(any(Pageable.class), eq(studentId), eq(null), eq(null), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/score_sheets")
                            .param("studentId", studentId.toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].assessment").value("EXCELLENT"));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden, если пользователь пытается просмотреть чужую оценку")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403WhenStudentViewsOtherScore() throws Exception {
            Long id = 1L;
            when(scoreSheetService.getScoreSheetById(id))
                    .thenThrow(new AccessDeniedException("Вы не можете просматривать чужие оценки"));

            mockMvc.perform(get("/api/v1/score_sheets/{id}", id))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если оценка не найдена")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            Long id = 999L;
            when(scoreSheetService.getScoreSheetById(id))
                    .thenThrow(new ResourceNotFoundException("Оценка по предмету не найдена", "ScoreSheet not found: id=" + id));

            mockMvc.perform(get("/api/v1/score_sheets/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Оценка по предмету не найдена"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/score_sheets/{id} - Обновление оценки")
    class UpdateScoreSheet {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении преподавателем")
        @WithMockUser(roles = "TEACHER")
        void shouldUpdateSuccessfullyForTeacher() throws Exception {
            Long id = 1L;
            ScoreSheetUpdateRequest request = createValidUpdateRequest();
            ScoreSheetResponse response = createTestResponse(id);

            when(scoreSheetService.updateScoreSheetById(eq(id), any(ScoreSheetUpdateRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/score_sheets/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/score_sheets/{id} - Удаление оценки")
    class DeleteScoreSheet {

        @Test
        @DisplayName("Должен вернуть 204 No Content при удалении преподавателем своего предмета")
        @WithMockUser(roles = "TEACHER")
        void shouldDeleteSuccessfully() throws Exception {
            Long id = 1L;
            doNothing().when(scoreSheetService).deleteScoreSheetById(id);

            mockMvc.perform(delete("/api/v1/score_sheets/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(scoreSheetService, times(1)).deleteScoreSheetById(id);
        }
    }
}