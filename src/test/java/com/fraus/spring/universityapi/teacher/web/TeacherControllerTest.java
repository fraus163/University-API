package com.fraus.spring.universityapi.teacher.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.teacher.domain.TeacherService;
import com.fraus.spring.universityapi.teacher.domain.db.AcademicDegreeType;
import com.fraus.spring.universityapi.teacher.domain.db.AcademicRankType;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherCreateRequest;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherUpdateRequest;
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

@WebMvcTest(TeacherController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование TeacherController")
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    private TeacherResponse createTestResponse(Long id) {
        return new TeacherResponse(
                id,
                "Сидоров",
                "Сидор",
                "Сидорович",
                "+79291234567",
                "teacher@university.com",
                (short) 5,
                AcademicRankType.ASSOCIATE_PROFESSOR,
                AcademicDegreeType.CANDIDATE_OF_SCIENCES,
                List.of()
        );
    }

    @Nested
    @DisplayName("POST /api/v1/teachers - Создание преподавателя")
    class CreateTeacher {

        @Test
        @DisplayName("Должен вернуть 201 Created при успешном создании админом")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateTeacherForAdmin() throws Exception {
            TeacherCreateRequest request = new TeacherCreateRequest(
                    10L,
                    (short) 5,
                    AcademicRankType.ASSOCIATE_PROFESSOR,
                    AcademicDegreeType.CANDIDATE_OF_SCIENCES,
                    List.of(1)
            );
            TeacherResponse response = createTestResponse(100L);

            when(teacherService.createTeacher(any(TeacherCreateRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/teachers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/teachers/100")))
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.lastName").value("Сидоров"))
                    .andExpect(jsonPath("$.academicRank").value("ASSOCIATE_PROFESSOR"));

            verify(teacherService, times(1)).createTeacher(any(TeacherCreateRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке создания студентом")
        @WithMockUser(roles = "STUDENT")
        void shouldReturn403ForStudentOnCreate() throws Exception {
            TeacherCreateRequest request = new TeacherCreateRequest(
                    10L, (short) 5, null, null, List.of(1)
            );

            mockMvc.perform(post("/api/v1/teachers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(teacherService, never()).createTeacher(any());
        }

        @Test
        @DisplayName("Должен вернуть 400 Bad Request при пустых обязательных полях")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400OnInvalidRequestBody() throws Exception {
            TeacherCreateRequest request = new TeacherCreateRequest(
                    null, (short) -1, null, null, List.of()
            );

            mockMvc.perform(post("/api/v1/teachers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(teacherService, never()).createTeacher(any());
        }

        @Test
        @DisplayName("Должен вернуть 400 Bad Request, если пользователь уже является преподавателем")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenUserAlreadyTeacher() throws Exception {
            TeacherCreateRequest request = new TeacherCreateRequest(
                    10L, (short) 5, null, null, List.of(1)
            );

            when(teacherService.createTeacher(any(TeacherCreateRequest.class)))
                    .thenThrow(new InvalidValueException("Пользователь уже является преподавателем", "User with id=10 is already a teacher"));

            mockMvc.perform(post("/api/v1/teachers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Пользователь уже является преподавателем"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/teachers - Получение преподавателей")
    class GetTeachers {

        @Test
        @DisplayName("Должен вернуть 200 OK и данные преподавателя по ID")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnTeacherById() throws Exception {
            Long id = 100L;
            TeacherResponse response = createTestResponse(id);

            when(teacherService.getTeacherById(id)).thenReturn(response);

            mockMvc.perform(get("/api/v1/teachers/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.lastName").value("Сидоров"))
                    .andExpect(jsonPath("$.email").value("teacher@university.com"));
        }

        @Test
        @DisplayName("Должен вернуть 404 Not Found, если преподаватель не найден")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn404WhenNotFound() throws Exception {
            Long id = 999L;
            when(teacherService.getTeacherById(id))
                    .thenThrow(new ResourceNotFoundException("Преподаватель не найден", "Teacher not found: id=" + id));

            mockMvc.perform(get("/api/v1/teachers/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Преподаватель не найден"));
        }

        @Test
        @DisplayName("Должен вернуть 200 OK и страницу преподавателей по фильтру")
        @WithMockUser(roles = "STUDENT")
        void shouldReturnPageByFilter() throws Exception {
            TeacherResponse response = createTestResponse(100L);
            Page<TeacherResponse> page = new PageImpl<>(List.of(response));

            when(teacherService.getTeachersByFilter(any(Pageable.class), eq("Сидоров"), eq(null), eq(null), eq(null), eq(null), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/teachers")
                            .param("lastName", "Сидоров")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(100))
                    .andExpect(jsonPath("$.content[0].lastName").value("Сидоров"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/teachers/{id} - Обновление преподавателя")
    class UpdateTeacher {

        @Test
        @DisplayName("Должен вернуть 200 OK при успешном обновлении админом")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateTeacherForAdmin() throws Exception {
            Long id = 100L;
            TeacherUpdateRequest request = new TeacherUpdateRequest(
                    (short) 10,
                    AcademicRankType.PROFESSOR,
                    AcademicDegreeType.DOCTOR_OF_SCIENCES,
                    List.of(2)
            );
            TeacherResponse response = createTestResponse(id);

            when(teacherService.updateTeacherById(eq(id), any(TeacherUpdateRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/teachers/{id}", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100));
        }

        @Test
        @DisplayName("Должен вернуть 403 Forbidden при попытке обновления обычным преподавателем")
        @WithMockUser(roles = "TEACHER")
        void shouldReturn403ForTeacherOnUpdate() throws Exception {
            TeacherUpdateRequest request = new TeacherUpdateRequest((short) 10, null, null, List.of(2));

            mockMvc.perform(put("/api/v1/teachers/{id}", 100L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(teacherService, never()).updateTeacherById(any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/teachers/{id} - Удаление преподавателя")
    class DeleteTeacher {

        @Test
        @DisplayName("Должен вернуть 204 No Content при успешном удалении админом")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteTeacherForAdmin() throws Exception {
            Long id = 100L;
            doNothing().when(teacherService).deleteTeacherById(id);

            mockMvc.perform(delete("/api/v1/teachers/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(teacherService, times(1)).deleteTeacherById(id);
        }
    }
}